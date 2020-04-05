import program from 'commander';
import pkg from './../package.json';
import pify from 'pify';
import {copy} from 'copy-paste';
import {getByName, getByFile, getByUrl} from './';

const handleOutput = result => {
  const message = program.json ? JSON.stringify(result) : result.join('\n\r');

  console.log(message);

  return program.copy ? pify(copy)(message) : Promise.resolve();
};

program.version(pkg.version)
  .description('Get the dependencies of a package.json')
  .usage('[options] [path]')
  .option('-m, --module <name>', 'Get the package.json of a npm module')
  .option('-u, --url <url>', 'Get the package.json from a url')
  .option('-j, --json', 'Print the dependencies as a JSON')
  .option('-c, --copy', 'Copy the dependencies to the clipboard');

module.exports = argv => {
  program
    .parse(argv);

  if (program.module) {
    return getByName(program.module)
      .then(handleOutput)
      .catch(err => console.error('Could not retrieve the requested package.json file', err));
  }

  if (program.url) {
    return getByUrl(program.url)
      .then(handleOutput)
      .catch(err => console.error('Could not retrieve the requested package.json file', err));
  }

  return getByFile(program.args.shift() || process.cwd())
    .then(handleOutput)
    .catch(err => console.error('Could not read the required file', err));
};
