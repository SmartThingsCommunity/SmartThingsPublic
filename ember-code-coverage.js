const { getInput, setFailed } = require('@actions/core');
const { exec } =  require('@actions/exec');
const { GitHub, context} = require('@actions/github');
const fs = require('fs');
const { buildOutput } = require('./lib/build-output');

let octokit;
const repoInfo = context.repo;

async function run() {
  try {
    let myToken = getInput('repo-token', { required: true });
    let testCommand = getInput('test-command', { required: true });
    let coverageFilePath = getInput('coverage-file', { required: true });
    let coverageIndicator = getInput('coverage-indicator', { required: true });
    let workingDirectory = getInput('working-directory', { required: true });

    octokit = new GitHub(myToken);
    let pullRequest = await getPullRequest();

    let testCoverage = await getTestCoverage({ testCommand, coverageFilePath, coverageIndicator, workingDirectory });

    console.log(`
    
New test coverage: ${testCoverage}%
    
    `);

    await exec(`git fetch origin ${pullRequest.base.sha}`);
    await exec(`git checkout ${pullRequest.base.sha}`);

    // This could fail, e.g. if no test coverage existed before
    let testCoverageBefore;
    try {
      testCoverageBefore = await getTestCoverage({testCommand, coverageFilePath, coverageIndicator, workingDirectory});
    } catch (error) {
      testCoverageBefore = 0;
    }

    console.log(`
    
Previous test coverage: ${testCoverageBefore}%
    
    `);

    let body = buildOutput({ testCoverage, testCoverageBefore });

    try {
      await octokit.issues.createComment({
        owner: context.repo.owner,
        repo: context.repo.repo,
        issue_number: pullRequest.number,
        body,
      });
    } catch (e) {
      console.log(`Could not create a comment automatically. This could be because github does not allow writing from actions on a fork.
See https://github.community/t5/GitHub-Actions/Actions-not-working-correctly-for-forks/td-p/35545 for more information.`);

      console.log(`Copy and paste the following into a comment yourself if you want to still show the diff:
${body}`);
    }
  } catch (error) {
    setFailed(error.message);
  }
}

async function getTestCoverage({ testCommand, coverageFilePath, coverageIndicator, workingDirectory }) {
  await exec(testCommand,[],{cwd:workingDirectory});

  let coverageFile = fs.readFileSync(coverageFilePath, 'utf-8');
  let coverageSummary = JSON.parse(coverageFile);

  return coverageSummary.total[coverageIndicator].pct;
}

async function getPullRequest() {
  let pr = context.payload.pull_request;

  if (!pr) {
    console.log('Could not get pull request number from context, exiting');
    return;
  }

  const { data: pullRequest } = await octokit.pulls.get({
    owner: pr.base.repo.owner.login,
    repo: pr.base.repo.name,
    pull_number: pr.number
  });

  return pullRequest;
}

run();