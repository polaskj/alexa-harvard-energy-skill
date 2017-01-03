# alexa-harvard-energy-skill
This repository is an Alexa skill which is an audible representation of this page: http://www.energyandfacilities.harvard.edu/real-time-energy-production-demand
###You can say things like:

* Ask Harvard what it's electricity demand is
* Ask Harvard about their blackstone electricity production
* Ask Harvard about their steam production
* Ask Harvard about it's chilled water production

(additional utterances can be added to Skill configuration.)

### Running Locally
* Set up local certificate: https://developer.amazon.com/public/solutions/alexa/alexa-skills-kit/docs/deploying-a-sample-skill-as-a-web-service 
* Resolve and package jar: `mvn assembly:assembly -DdescriptorId=jar-with-dependencies package`
* Launch jar with embedded Jetty: `mvn exec:java -Dexec.executable=”java” -DdisableRequestSignatureCheck=true`

* Alternatively, resolve dependencies using Maven, and launch edu.harvard.Launcher.java 

Hit endpoint:```curl -v -k https://localhost:8888/energy --data-binary  '{
  "version": "1.0",
  "session": {
    "new": true,
    "sessionId": "session1234",
    "application": {
      "applicationId": "amzn1.echo-sdk-ams.app.1234"
    },
    "attributes": {},
    "user": {
      "userId": null
    }
  },
  "request": {
    "type": "LaunchRequest",
    "requestId": "request5678",
    "timestamp": "2015-05-13T12:34:56Z"
  }
}'```

### Deploy / Enable on Echo
* Add a new Skill in the developer.amazon.com, and add the ApplicationID generated to `edu.harvard.EnergyProductionSpeechletRequestStreamHandler`
* Build the project via Maven: `mvn assembly:assembly -DdescriptorId=jar-with-dependencies package`, which generates `alexa-harvard-energy-skill-0.1-jar-with-dependencies.jar`
* Upload the jar to an S3 bucket
* Create an AWS Lamda using the uploaded artifcact
* Populate the Skill's' configuration using Intent Schema, Custom Slot Type, and Utterances from the `edu.harvard.resources` package, in this project.
* Use the Test simulator or Echo associated to your developer account to test functionality. 
