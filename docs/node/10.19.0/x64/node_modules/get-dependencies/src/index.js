import _ from 'lodash';
import pify from 'pify';
import jsonfile from 'jsonfile';
import fetchPackage from 'package-json';
import got from 'got';

const formatData = data => _.keys(data.dependencies).concat(_.keys(data.devDependencies));

export function getByFile (filePath) {
  return pify(jsonfile).readFile(filePath)
    .then(formatData);
}

export function getByName (name) {
  return fetchPackage(name, 'latest')
    .then(formatData);
}

export function getByUrl (url) {
  return got(url, {json: true})
    .then(({body}) => body)
    .then(formatData);
}
