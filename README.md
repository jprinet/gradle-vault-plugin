# gradle-vault-plugin

this Gradle plugin allows to hide secrets in the code repository.

In details, all entries @@vault.MY-KEY@@ in a given subtree (_conf/*.properties_ by default) will be replaced by their secret value when the **processSecrets** task is run.

## Description

The plugin permits to create a vault (as a _.vault_ file by default), user is prompted for a passphrase to protect the vault.

The vault passphrase shouldn't be stored in the code repository for obvious reasons.

It is possible to create/retrieve/update secrets in this vault with the vault passphrase.

When assembling the code, the **processSecrets** task allows to convert tokens to their value from the vault with the passphrase (see Vault protection section).

When the code is packaged locally, the secrets are no longer hidden but the point is that they can't be read directly from the code repository without a valid passphrase.

The vault file belongs to the code repository, however secrets in there are **encrypted**.

## Vault protection

Any action on the vault needs a valid passphrase which can be transmitted through a VM argument _VAULT_PASSPHRASE_ or through a file (_.vault_passphrase_ by default). If using a file, make sure not to add it to the source code repository.

The passphrase is stored encrypted into the vault itself and must not be stored/exchanged in clear text.

The encryption/decryption mechanism is based on AES 256 + salt + Base64.

## Secret resolution

When the **processSecrets** task is triggered, all files matching the pattern (_'.\*/conf/.\*.properties'_ by default) in a configurable subtree (_build_ by default) are analyzed and each @@vault.MY-KEY@@ entries are replaced with the value associated to MY-KEY in the vault (given a valid access).

If some keys can't be resolved, they are added to the _.vault_missing_secrets_ file.

It is then possible to edit this file and set the missing passwords (identifier;password), it will be used as input by the _addMissingSecrets_ task to fill the vault. 

## Publish me!

the plugin can be published to your local repository:
```shell
./gradlew publishToMavenLocal
```

## Usage

Any plugin task requires the vault passphrase. The passphrase can be passed in 2 manners:
- content of .vault_passphrase
- as JVM option VAULT_PASSPHRASE

Just import the plugin into your build.gradle like this:

```groovy
buildscript {
    repositories {
        mavenLocal()
    }
    dependencies {
        classpath 'com.jprinet.gradle:gradle-vault-plugin:+'
    }
}

plugins {
    id 'com.jprinet.gradle.gradle-vault-plugin'
}
```

And the optional configuration block (if you're ok with the values below, no need to provide it):

```groovy
vault {
    vaultFile = '.vault'
    vaultPassphraseFile = '.vault_passphrase'
    resourcePath = 'build'
    resourcePattern = '.*/conf/.*.properties'
}
```

| Property            | Description                                              | Default                       |
| ------------------- | -------------------------------------------------------- | ----------------------------- |
| vaultFile           | vault file                                               | .vault                        |
| vaultPassphraseFile | file holding vault passphrase                            | .vault_passphrase             |
| resourcePath        | base path to look for resources                          | build                         |
| resourcePattern     | pattern to match to have templates replaced with secrets | .*/conf/.*.properties   |


### Tasks

- **createVault** : creates the vault

```groovy
gradlew createVault
```

- **addSecret** : adds a secret to the vault, the identifier/secret are read as command line inputs

```groovy
gradlew addSecret
```

- **addMissingSecrets** : adds all secret found in .vault_missing_secrets to the vault, the file format being lines of identifier;secret

```groovy
gradlew addMissingSecrets
```

- **removeSecret** : removes a secret from the vault, the identifier is read as command line input

```groovy
gradlew removeSecret
```

- **revealSecret** : reveals a secret from the vault, the identifier is read as command line input

```groovy
gradlew revealSecret
```

- **processSecrets** : replaces tokens with secrets

```groovy
gradlew processSecrets
```
