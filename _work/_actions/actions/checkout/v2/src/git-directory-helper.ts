import * as core from '@actions/core'
import * as fs from 'fs'
import * as fsHelper from './fs-helper'
import * as io from '@actions/io'
import * as path from 'path'
import {IGitCommandManager} from './git-command-manager'

export async function prepareExistingDirectory(
  git: IGitCommandManager | undefined,
  repositoryPath: string,
  repositoryUrl: string,
  clean: boolean
): Promise<void> {
  let remove = false

  // Check whether using git or REST API
  if (!git) {
    remove = true
  }
  // Fetch URL does not match
  else if (
    !fsHelper.directoryExistsSync(path.join(repositoryPath, '.git')) ||
    repositoryUrl !== (await git.tryGetFetchUrl())
  ) {
    remove = true
  } else {
    // Delete any index.lock and shallow.lock left by a previously canceled run or crashed git process
    const lockPaths = [
      path.join(repositoryPath, '.git', 'index.lock'),
      path.join(repositoryPath, '.git', 'shallow.lock')
    ]
    for (const lockPath of lockPaths) {
      try {
        await io.rmRF(lockPath)
      } catch (error) {
        core.debug(`Unable to delete '${lockPath}'. ${error.message}`)
      }
    }

    try {
      // Checkout detached HEAD
      if (!(await git.isDetached())) {
        await git.checkoutDetach()
      }

      // Remove all refs/heads/*
      let branches = await git.branchList(false)
      for (const branch of branches) {
        await git.branchDelete(false, branch)
      }

      // Remove all refs/remotes/origin/* to avoid conflicts
      branches = await git.branchList(true)
      for (const branch of branches) {
        await git.branchDelete(true, branch)
      }

      // Clean
      if (clean) {
        if (!(await git.tryClean())) {
          core.debug(
            `The clean command failed. This might be caused by: 1) path too long, 2) permission issue, or 3) file in use. For futher investigation, manually run 'git clean -ffdx' on the directory '${repositoryPath}'.`
          )
          remove = true
        } else if (!(await git.tryReset())) {
          remove = true
        }

        if (remove) {
          core.warning(
            `Unable to clean or reset the repository. The repository will be recreated instead.`
          )
        }
      }
    } catch (error) {
      core.warning(
        `Unable to prepare the existing repository. The repository will be recreated instead.`
      )
      remove = true
    }
  }

  if (remove) {
    // Delete the contents of the directory. Don't delete the directory itself
    // since it might be the current working directory.
    core.info(`Deleting the contents of '${repositoryPath}'`)
    for (const file of await fs.promises.readdir(repositoryPath)) {
      await io.rmRF(path.join(repositoryPath, file))
    }
  }
}
