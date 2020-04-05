# get-dependencies ![BuildStatus](https://travis-ci.org/SharonGrossman/get-dependencies.svg?branch=master) ![BuildStatus](https://ci.appveyor.com/api/projects/status/xgt19hyn77a8wcgy?svg=true)

## Install
as a CLI tool
```
npm install --g get-dependencies
```

Programatically
```
npm install get-dependencies --save
```

## Usage
### CLI
``` 
Usage: get-dependencies [options] [path]

  Get the dependencies of a package.json

  Options:

    -h, --help           output usage information
    -V, --version        output the version number
    -m, --module <name>  Get the package.json of a npm module
    -u, --url <url>      Get the package.json from a url
    -j, --json           Print the dependencies as a JSON
    -c, --copy           Copy the dependencies to the clipboard

```
### Programatically

```
var getDeps = require('get-dependencies');

getDeps.getByFile('/path/package.json')
  .then(function (result) {
    // result is an array of the dependencies
  });
  
getDeps.getByName('package-name')
  .then(function (result) {
    // result is an array of dependencies of the npm package
  });
  
getDeps.getByUrl('http://url.to/package.json')
  .then(function (result) {
    // result is an array of dependencies of the npm package
  });

```


## License

[MIT](LICENSE) © [Sharon Grossman](https://github.com/sharongrossman)
