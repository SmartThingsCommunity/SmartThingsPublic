# V2 Release Notes

## Summary of major changes

Version 2.0.0 is a major new release that is not 100% backwardly compatible with the 1.X version, though for most
SmartApps the changes required should be minor. Here are the areas where your app may need to be changed 
to use the V2 SDK:

### List methods now return arrays rather than objects with an items property
Methods that return lists now return arrays rather that an object with the properties `items` and `_links`. Paging 
is done automatically by the API. The 1.X version of the SDK does not support paging at all in these methods, which 
can result in missed items, such as when a location has more than 200 devices.

This change was also made for consitency, since not all endpoints used the `items`
object, and for convenience. Since the lists are of limited length and unordered,
paging is of limited practical use. Most applications, such as displaying a sorted display of items require all pages 
to be retrieved. In the future methods with potentially unbounded numbers of items with inherient order, such as event 
history, will include a paging mechanism.

### REST API calls are now handled by a separate package
The [@smartthings/core-sdk](https://www.npmjs.com/package/@smartthings/core-sdk) package is now used 
for all API calls. It can also be used in applications that are not SmartApps or API Access apps and
therefore don't need to use this SDK.

### There's a new HTTP client
As a result of the switch to use the [@smartthings/core-sdk](https://www.npmjs.com/package/@smartthings/core-sdk)
for REST API calls, [Axios](https://www.npmjs.com/package/axios) is now used as the HTTP client rather than 
[request-promise-native](https://www.npmjs.com/package/request-promise-native) (which has been deprecated). As a result, the error object has changed. It's now an instance of the 
[AxiosError](https://github.com/axios/axios/blob/master/index.d.ts#L76-L92) class, so instead of accessing the 
status of an error response with `.catch(error => {error.statusCode})` you'd do so with
`.catch(error => {error.response.status})`.

### All methods return a Promise
All methods now return a Promise. Before there were some methods that returned void. There was also inconsistency 
in the response of methods that returned no data, with some returning an empty obhect `{}`, others returning an
empty string `"`, and still others returning a status value `{"status": "success"}`. Now all such methods return 
a status value.

## Installation

The normal method of installation will install the latest V2 version of the SDK.
```bash
npm install @smartthings/smartapp
```
If you need to install the 1.X version for some reason you can do so with:
```bash
npm install @smartthings/smartapp@^1.18.0
```

##Usage

In-general you set up the SmartApp in the same was as before, as described in the 
[README file](../README.md). All of the same configuration methods are supported. The major difference
with the 1.X version is in methods that return lists. 

With the 1.X version of the SDK and object is returned with an `items` property that is an array of the 
items in the list and a `_links` proper with a url for the next page of items if there were more than
200 items in the list. 
```javascript
const result = context.api.devices.list()
for (const item of result.items) {
    // ...
}
```
With the V2.X SDK the query returns the array of items:
```javascript
const items = context.api.devices.list()
for (const item of items) {
    // ...
}
```

