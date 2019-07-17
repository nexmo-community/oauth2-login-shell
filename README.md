# oauth2-login-shell
This is a command-line tool to test authenticating against an OAuth2 authorization server. It was specifically designed as an example for Vonage's UC Extend Developer Portal (developer.vonage.com/store), but it should work other OAuth2 authorization servers too. It also serves as an example for those who want to develop desktop applications against the UC Extend APIs by using localhost as the OAuth2 callback url.

# Dependencies
Java 8 and maven

# Get and run the source code
1. Clone this repo.
2. Edit the `src/main/resources/application.properties` file to use your own client id and client secret you obtained via the Vonage UC Extend Developer Portal.
3. Run on the command-line via `mvn spring-boot:run`.
4. At the prompt, run the `login` command; it should launch a web browser window that redirects to a login page.
5. Login in with either valid Vonage Business Cloud or Vonage Business Enterprise credentials. It should display the access and refresh tokens on the console.

# Issues and contributions
We welcome comments at `devsupport@vonage.com` or Issues filed here on github.
