# Fullstack Developer Exercise
## Tech Company Social Media Rest API 

A backend for a social media platform.
- Spring framework 
- ✨Magic ✨

## Features

- Creating and Deleting a Post 
- Liking a post
- Commenting to a Post
- Application of Virtual Threads
- Dockirizing the Application



## Installation

Social media backed Api requires [java](https://openjdk.org/projects/jdk/21/) JDK 21+ to run.

Install the dependencies and devDependencies and start the server.

```sh
git clone https://github.com/AnoNelson/bk_challange
Open the project in any IDE (Preferable intellij IDEA)
Run mvn install
```
## Run and Check
- OpenAPI Specification - Version 2.0

```sh
Run Spring Boot project. Open browser with url:
http://localhost:<<port>>/swagger-ui/index.html
```
## Plugins

Dillinger is currently extended with the following plugins.
Instructions on how to use them in your own application are linked below.

| Plugin | README |
| ------ | ------ |
| spring security | [https://spring.io/projects/spring-security]|
| Swagger UI | [https://github.com/springfox/springfox-swagger-ui-rfc6570/blob/master/README.md]|
| lombok | [https://github.com/projectlombok/lombok/readme]|



#### Building for source

For production release:

```sh
mvn deploy
```

Generating Jar file skipping tests:

```sh
mvn -DskipTests=true  package
```

## Docker

spring-bk-challenge is very easy to install and deploy in a Docker container.

By default, the Docker will expose port 8080, so change this within the
Dockerfile if necessary. When ready, simply use the Dockerfile to
build the image.

```sh
cd bk_challenge
docker build -t <youruser>/spring-bk-challenge:${package.json.version} .
```

This will create the spring-bk-challenge image and pull in the necessary dependencies.
Be sure to swap out `${package.json.version}` with the actual
version of spring-bk-challenge.

Once done, run the Docker image and map the port to whatever you wish on
your host. In this example, we simply map port 8000 of the host to
port 8080 of the Docker (or whatever port was exposed in the Dockerfile):

```sh
systemctl start docker
```
```sh
docker network create challenge-mysql
```
```sh
docker container run --name mysqldb --network challenge-mysql -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=social_media -d mysql
```
> Note: `--Docker` must be installed.

Verify the deployment by navigating to your server address in
your preferred browser.

```sh
localhost:8000
```

## License

Nelson Ishimwe

**Free Software, Hell Yeah!**
