[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
# BuyerRemover
Removes buyers from a resource on the Spigot forums or other Xenforo forums using bdPayGates. You must be the creator of the resource to use this.
To use this program, simply lookup the resource id (You can find this in the url) and the buyer ids (You can find those in the profile url).

## CLI
This program can be used by typing `java -jar BuyerRemover.jar <url> <username> <password> <resource id> <target id 1> <target id 2>...` in your CLI

## Library
You can also use this as a library in your plugins or other projects, by importing the jitpack maven project.
To do this add the following:

Repository:
```xml
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>
```

Dependency:
```xml
<dependency>
  <groupId>com.github.JoachimVandersmissen</groupId>
  <artifactId>BuyerRemover</artifactId>
  <version>Build-{build-number-here}</version>
</dependency>
```

After you've done that, you can import `com.joachimvandersmissen.BuyerRemover`  
To remove buyers, first you have to create a new `BuyerRemover` instance, and execute the `login` method. Afterwards, you can just execute the `removeBuyer` method.  
You only have to log in once per instance.

## Disclaimer
For educational purposes only. Don't use this without explicit permission of the site administrator and the buyers.  
Nothing is stored, no usernames, no passwords, no cookies. Feel free to check the code.