# java-lambda-smartapp ![java-logo](../../docs/java-logo.png) ![aws-logo](../../docs/aws-logo.png)

This sample demonstrates a simple [Java](https://www.oracle.com/java/) server using
[AWS Lambdas](https://aws.amazon.com).

## Requirements

* Java 1.8
* [SmartThings developer](https://smartthings.developer.samsung.com/workspace/) account
* [Amazon Web Services](https://aws.amazon.com/) account

## Installing the Example

Follow the steps below to install and run this sample SmartApp.

1. Install the [AWS CLI](https://docs.aws.amazon.com/cli/latest/userguide/cli-chap-install.html).
2. Configure the CLI to use your account.

    1. Get an access key to use when deploying and configuring your SmartApp.

        1. Find "My Security Credentials" under your account settings.
        2. Expand "Access keys (access key ID and secret access key)".
        3. If you already have an access key _and_ you know the secret for it, you can use that one.
        4. Otherwise, create a new key and note the id and secret.

    1. Run `aws configure` and populate as follows:

    | Option                | Value                               |
    |-----------------------|-------------------------------------|
    | AWS Access Key ID     | id from above                       |
    | AWS Secret Access Key | secret from above                   |
    | Default region name   | choose an [AWS region](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/Concepts.RegionsAndAvailabilityZones.html) geographically close to your end-user's location |
    | Default output format | enter 'json' here                   |

3. Create a role for the lambda to use. You'll find "Roles" under "My Security Credentials".
   Create the role with the following options:


    | Option    | Value                             |
    |-----------|-----------------------------------|
    | Service   | Lambda                            |
    | Policy    | AWSLambdaFullAccess               |
    | Tags      | None                              |
    | Role Name | Something like "LambdaFullAccess" |

3. To build the zip file, un the following command from the root project directory:

    ```
    ./gradlew examples:java-lambda-smartapp:buildZip
    ```

1. Deploy the zip file by running the following command. Be sure to update your
   [account id](https://docs.aws.amazon.com/IAM/latest/UserGuide/console_account-alias.html)
   and the version.

    ```
    aws lambda create-function --function-name smartapp-example \
        --zip-file fileb://build/distributions/java-lambda-smartapp-<version>.zip \
        --role arn:aws:iam::<account id>:role/lambda_full_access \
        --handler app.App \
        --runtime java8 \
        --timeout 15 \
        --memory-size 256
    ```

    Later, when you want to deploy an updated zip file, use `aws lambda update-function-code` instead:

    ```
    aws lambda update-function-code --function-name smartapp-example \
        --zip-file fileb://build/distributions/java-lambda-smartapp-<version>.zip
    ```

    (If you need to update configuration later, use the `aws lambda update-function-configuration`.)

4. Add permissions for SmartThings access to Lambda. This gives SmartThings (which has a principle
    id of 906037444270) permission to call the Lambda.

    ```
    aws lambda add-permission \
        --function-name <my-function-name> \
        --statement-id smartthings \
        --principal 906037444270 \
        --action lambda:InvokeFunction
    ```

### Create SmartApp

First, you'll need to determine the target ARN for the function you just deployed. Find the
function you just deployed in the AWS Console and you will find the ARN in the upper right
corner.

Then, follow these steps using the
[developer workspace](https://smartthings.developer.samsung.com/workspace/):

  1. Create a new project and choose "Automation for the SmartThings App".
  1. Give your project a unique name.
  1. Choose "Automation Connector | SmartApp" under "Develop" in the left-hand menu.
  1. Choose "AWS Lambda" and enter the target ARN you found above.
  1. Select the `r:devices:*` and `x:devices:*` scopes and click "Next".
  1. Give your application a name and hit "Save".
  1. Save the new project and click the "Deploy to Test" button.

### Install SmartApp

First, be sure to enable
[developer mode](https://smartthings.developer.samsung.com/docs/guides/testing/developer-mode.html#Enable-Developer-Mode)
in your SmartThings application to see the self-published automation.

Now you should see your SmartApp listed (near the bottom) when you add a new Automation.

There are lots of sample log messages in this example. You should see the results of this
in the AWS console. Look for "View logs in CloudWatch" under the "Monitoring" tab of the
function.

## References

- [SmartThings Developer Workspace](https://devworkspace.developer.samsung.com)
- [Hosting with AWS Lambda](https://smartthings.developer.samsung.com/docs/guides/smartapps/aws-lambda.html)
- [AWS CLI](https://docs.aws.amazon.com/cli/latest/userguide/cli-chap-install.html)
- [AWS Lambda](https://aws.amazon.com/lambda/)
- [Lambda Functions in Java](https://docs.aws.amazon.com/lambda/latest/dg/java-programming-model.html)
- [AWS Lambda Execution Role](https://docs.aws.amazon.com/lambda/latest/dg/lambda-intro-execution-role.html)

## More about SmartThings

If you are not familiar with SmartThings, we have
[extensive on-line documentation](https://smartthings.developer.samsung.com/develop/index.html).

To create and manage your services and devices on SmartThings, create an account in the
[developer workspace](https://devworkspace.developer.samsung.com/).

The [SmartThings Community](https://community.smartthings.com/c/developers/) is a good place share and
ask questions.

There is also a [SmartThings reddit community](https://www.reddit.com/r/SmartThings/) where you
can read and share information.

## License and Copyright

Licensed under the [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0)

Copyright 2019 SmartThings, Inc.
