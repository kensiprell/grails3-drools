## Under development. Do not use.

## Grails 3 plugin for integrating Drools

[Drools](https://www.drools.org) is a Business Rules Management System (BRMS) solution. The plugin fully supports Drools [kie-spring](https://docs.jboss.org/drools/release/6.2.0.Final/drools-docs/html/ch.kie.spring.html) integration.

This plugin only works with Grails versions greater than 3.0. The [original plugin](http://grails.org/plugin/drools) works with Grails 2.x. 

The plugin has been tested using the [sample application](https://github.com/kensiprell/grails3-drools-sample) in the following environment:

* Drools 6.2.0.Final

* Grails 3.0.0.RC2

* OSX 10.10.2

* JDK 1.7.0_75

If you have a question, problem, suggestion, or want to report a bug, please submit an [issue](https://github.com/kensiprell/grails3-drools/issues). I will reply as soon as I can.

[Release Notes](https://github.com/kensiprell/grails3-drools/wiki/Release-Notes)

## How the Plugin Works
The plugin offers a variety of ways to use rules. The [RuleTests](https://github.com/kensiprell/grails3-drools/blob/master/src/integration-test/groovy/grails/plugin/drools/RulesTestsSpec.groovy) and [TestController](https://github.com/kensiprell/grails3-drools-sample/blob/master/grails-app/controllers/com/plugin/drools/TestController.groovy) classes show several examples.

### Beans
You can define beans using either a configuration file `grails-app/conf/DroolsConfig.groovy` or an xml file `grails-app/conf/drools-context.xml`. This will allow you to do something like this:

    class SomeService {
        def packageOneStatelessSession

        def someMethod() {
               def fact1 = SomeDomain.get(123)
               def fact2 = SomeOtherDomain.get(123)
               def facts = [fact1, fact2]
               packageOneStatelessSession.execute(facts)
        }
    }

### Database and File Rules
The [DroolsService](https://github.com/kensiprell/grails3-drools/blob/master/grails-app/services/grails/plugin/drools/DroolsService.groovy) offers several methods to use rules stored in a database or on the file system (classpath). For example, assuming you have several rules in the database with a `packageName` of "ticket", you could do something like this:

    class SomeOtherService {
        def droolsService

        def someMethod() {
            def fact1 = SomeDomain.get(123)
            def fact2 = SomeOtherDomain.get(123)
            droolsService.fireFromDatabase("ticket", [fact1, fact2])
        }
    }

## Plugin Installation and Configuration

### Installation
Edit your application's `build.gradle` file. The following example shows the minimum required settings and omits the non-plugin relevant options. See the plugin sample application's [build.gradle](https://github.com/kensiprell/grails3-drools-sample/blob/master/build.gradle) for a commented example.

    buildscript {
        ext {
           droolsPluginVersion = "1.0.0"
        }
        dependencies {
           classpath "org.grails.plugins:drools:$droolsPluginVersion:gradle-plugin@jar"
        }
    }

    apply plugin: "grails3-drools"

    dependencies {
        compile "org.grails.plugins:drools:$droolsPluginVersion"
    }

    tasks.processResources.dependsOn(copyDroolsRule, writeDroolsContentXml)

You can change the default settings for the following items.

* droolsConfigurationType: Whether to use a Groovy or XML file to define the Drools beans. The default is `droolsConfigGroovy`, which uses `grails-app/conf/DroolsConfig.groovy` to generate `grails-app/conf/drools-context.xml`. Using `droolsContextXml` will not overwrite `grails-app/conf/drools-context.xml`, allowing you to edit it directly.

* droolsDrlFileLocation: Where the application's *.drl and *.rule files are stored. The default is `src/rules`.

* droolsRuleDomainClass: If you want to store rules in the database, this setting is used by the plugin's `DroolsService` to find rules based on package names. There is no default. The `create-drools-domain` script will update this setting automatically.

* Drools dependencies: The plugin offers an easy way to change one or more of the Drools dependency versions by adding a few lines to your `build.gradle` file.

#### droolsConfigurationType
The option below will stop the plugin from overwriting `grails-app/conf/drools-context.xml`. This will allow you to edit the file manually without losing changes.

    buildscript {
        ext {
           droolsConfigurationType = "droolsContextXml"
        }
    }

#### droolsDrlFileLocation
This option defines the directory root for rule files, those files with a "drl" or "rule" suffix. You will have to add the property below to your application's `gradle.properties` file. Note the lack of leading and trailing slashes below:

    // gradle.properties
    droolsDrlFileLocation="src/main/droolsRules"

You will also have to add `droolsDrlFileLocation = project.droolsDrlFileLocation` to your application's `build.gradle` file.

    // build.gradle
    buildscript {
        ext {
           droolsDrlFileLocation = project.droolsDrlFileLocation
        }
    }

#### droolsRuleDomainClass
If you change the domain class used to store your rules without using the script `create-drools-domain`, you will have to edit the corresponding configuration option in your application's `grails-app/conf/application.yml` file.

    grails:
        plugin:
            drools:
                droolsRuleDomainClass: com.example.DroolsRule

#### Drools Dependencies
The plugin allows you to change any or all of the Drools dependency versions by adding the lines below to your `build.gradle` file. For example, if you only want to change the XStream version, you would only need the `xstreamVersion` line. The lines in the `repositories` and `dependencies` blocks are required. The versions the plugin uses can be found in the [Gradle plugin](https://github.com/kensiprell/grails3-drools/blob/master/buildSrc/src/main/groovy/grails/plugin/drools/DroolsGradlePlugin.groovy#L9-L15).

    buildscript {
        ext {
            // Change any combination of the settings below.
            droolsVersion = "6.2.0.Final"
            comSunXmlBindVersion = "2.2.11"
            janinoVersion = "2.7.5"
            xstreamVersion = "1.4.7"
            ecjVersion = "4.4"
            mvelVersion = "2.2.2.Final"
            antlrRuntimeVersion = "3.5.2"
        }
    }

    repositories {
        maven { url "https://repository.jboss.org/nexus/content/repositories/public" }
    }

    dependencies {
        compile droolsPluginCompile
        runtime droolsPluginRuntime
    }

### DroolsConfig.groovy
Although Grails prefers convention over configuration, you cannot avoid some configuration for the Drools beans.

After the plugin is installed you will find a heavily commented [DroolsConfig.groovy](https://github.com/kensiprell/grails3-drools/blob/master/src/templates/conf/DroolsConfig.groovy) in `grails-app/conf/` that you can use as a starting point for configuring your beans. When your application is compiled this file is parsed and `grails-app/conf/drools-context.xml` is created (or overwritten). 

See the [DroolsConfig.groovy](https://github.com/kensiprell/grails3-drools/blob/master/src/templates/conf/DroolsConfig.groovy) template mentioned above for configuration options and instructions. [Drools Spring Integration](http://docs.jboss.org/drools/release/6.2.0.Final/drools-docs/html/ch.kie.spring.html) provides more information.

## Using the Plugin

### Drools Rule Files

You can take advantage of rule packages by creating subdirectories under `droolsDrlFileLocation`. See the plugin's [src/rules directory](https://github.com/kensiprell/grails3-drools/tree/master/src/rules) for examples.

Changing the `droolsDrlFileLocation` option will affect the `packages` property for a `KieBase`. For example, for the option

    droolsDrlFileLocation="src/main/droolsRules"

with a rule file located on the file system at

    ~/my-grails-app/src/main/droolsRules/packageOne/ruleFile1.drl

The rule file will be avaiable on the classpath as

    droolsRules.packageOne.ruleFile1.drl

and the KieBase packages property would be

    packages: "droolsRules.packageOne"

### Scripts
The plugin offers three command-line scripts.

#### create-drools-config
Running `grails create-drools-config` will copy the default [DroolsConfig.groovy](https://github.com/kensiprell/grails3-drools/blob/master/src/main/templates/conf/DroolsConfig.groovy) to your application's `grails-app/conf/` directory.

#### create-drools-context
Running `grails create-drools-context` will copy the default [drools-context.xml](https://github.com/kensiprell/grails3-drools/blob/master/src/main/templates/conf/drools-context.xml) to your application's `grails-app/conf/` directory.

#### create-drools-domain
Running `grails create-drools-domain` will create a domain class with a package and name of your choice. Use this class to store your rules in the database. For example, if you run the command below:

    grails create-drools-domain com.example.DroolsRule

It will create `grails-app/domain/com/example/DroolsRule.groovy` from a template and also add or update the configuration option below in your application's `grails-app/conf/application.yml` file:

    grails:
        plugin:
            drools:
                droolsRuleDomainClass: com.example.DroolsRule

