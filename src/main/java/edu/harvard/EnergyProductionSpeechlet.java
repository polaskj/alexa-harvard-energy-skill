/**
    Copyright 2014-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.

    Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with the License. A copy of the License is located at

        http://aws.amazon.com/apache2.0/

    or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package edu.harvard;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.Speechlet;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;

/**
 * This sample shows how to create a simple speechlet for handling intent requests and managing
 * session interactions.
 */
public class EnergyProductionSpeechlet implements Speechlet {
    private static final Logger log = LoggerFactory.getLogger(EnergyProductionSpeechlet.class);

    private static final String LAST_OPTION_KEY = "LAST_OPTION";
    private static final String POWER_SOURCE_SLOT = "PowerSource";
//    private static final Map<String, String> OPTION_TO_TAG_MAP;
//    static
//    {
//        OPTION_TO_TAG_MAP = new HashMap<String, String>();
//        OPTION_TO_TAG_MAP.put("Blackstone Electricity Production", "BlackStnTurboGen");
//        OPTION_TO_TAG_MAP.put("Chilled Water Production", "CombinedPlant.Tons.CUP");
//        OPTION_TO_TAG_MAP.put("Steam Production", "PLANT.STMOUT.Plant");
//        OPTION_TO_TAG_MAP.put("Total Electricity Demand", "System_ELE_HarvardPurchGenTotal_PowerReal_000_SUM");
//
//    }
    @Override
    public void onSessionStarted(final SessionStartedRequest request, final Session session)
            throws SpeechletException {
        log.info("onSessionStarted requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
        // any initialization logic goes here
    }

    @Override
    public SpeechletResponse onLaunch(final LaunchRequest request, final Session session)
            throws SpeechletException {
        log.info("onLaunch requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
        return getWelcomeResponse();
    }

    @Override
    public SpeechletResponse onIntent(final IntentRequest request, final Session session)
            throws SpeechletException {
        log.info("onIntent requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());

        // Get intent from the request object.
        Intent intent = request.getIntent();
        String intentName = (intent != null) ? intent.getName() : null;

        // Note: If the session is started with an intent, no welcome message will be rendered;
        // rather, the intent specific response will be returned.
        if ("GetEnergyData".equals(intentName)) {
            return getPowerSourceData(intent, session);
        }
// else if ("WhatsMyColorIntent".equals(intentName)) {
//            return getColorFromSession(intent, session);
//        }
        else {
            throw new SpeechletException("Invalid Intent");
        }
    }

    @Override
    public void onSessionEnded(final SessionEndedRequest request, final Session session)
            throws SpeechletException {
        log.info("onSessionEnded requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
        // any cleanup logic goes here
    }

    /**
     * Creates and returns a {@code SpeechletResponse} with a welcome message.
     *
     * @return SpeechletResponse spoken and visual welcome message
     */
    private SpeechletResponse getWelcomeResponse() {
        // Create the welcome message.
        String speechText =
                "Welcome to the Alexa Skills Kit sample. Please tell me your favorite color by "
                        + "saying, my favorite color is red";
        String repromptText =
                "Please tell me your favorite color by saying, my favorite color is red";

        return getSpeechletResponse(speechText, repromptText, true);
    }

    /**
     * Creates a {@code SpeechletResponse} for the intent and stores the extracted color in the
     * Session.
     *
     * @param intent
     *            intent for the request
     * @return SpeechletResponse spoken and visual response the given intent
     */
    private SpeechletResponse getPowerSourceData(final Intent intent, final Session session) {
        // Get the slots from the intent.
        Map<String, Slot> slots = intent.getSlots();

        // Get the color slot from the list of slots.
        Slot powerSourceSlot = slots.get(POWER_SOURCE_SLOT);
        String speechText, repromptText;
        JSONArray powerArray = null;
        Double dataPoint = null;
        try {
            String genreJson = IOUtils.toString(new URL("https://apps2.campusservices.harvard.edu/energy/api?callback=alexa").openStream());
            JSONTokener t = new JSONTokener(genreJson);
            t.nextValue();
            powerArray = (JSONArray) t.nextValue(); // skip the callback
        } catch (IOException e) {
            log.warn("Couldn't fetch data from API");
            return getSpeechletResponse("The energy and utility service is currently unavailable. Please try again later.", null, false);
        }


        String powerSource = powerSourceSlot.getValue();
        String dataKey = null;
        if (powerSource.equals("electricity demand")){
            speechText = "Currently, %s megawatts of electricity is being consumed within buildings, " +
                    "supplied through the energy and facilities micro-grid.";
            dataKey = "System_ELE_HarvardPurchGenTotal_PowerReal_000_SUM";
        } else if (powerSource.endsWith("electricity production")){
            speechText = "Currently %s mega-watts of electricity is being produced by the Blackstone Steam Plant’s 5.7 " +
                    "mega-watt back-pressure turbine.";
            dataKey = "BlackStnTurboGen";
        } else if (powerSource.equals("steam production")){
            speechText = "Currently, %s pound per hour of steam is being produced by the four boilers within " +
                    "the Blackstone Steam Plant on Western Avenue.";
            dataKey = "PLANT.STMOUT.Plant";
        } else if (powerSource.equals("water production")){
            speechText = "Currently, %s tons of chilled water is being produced by a 13,000 ton central plant on Oxford Street " +
                    "and a 7,500 ton plant in the Northwest Building.";
            dataKey = "CombinedPlant.Tons.CUP";
        } else {
            speechText = "Sorry I didn't understand.";
            dataKey = "BlackStnTurboGen";
        }


        if (powerArray != null) {
            for (int i = 0; i < powerArray.length(); i++) {
                JSONObject item = powerArray.getJSONObject(i);
                if (item.getString("name").equals(dataKey)) {
                    dataPoint = item.getDouble("value");
                    break;
                }
            }
        }


        speechText = String.format(speechText,dataPoint);
        repromptText =
                "You can ask me about electricity demand and production. Please repeat your question.";
        return getSpeechletResponse(speechText, repromptText, false);
    }

    /**
     * Creates a {@code SpeechletResponse} for the intent and get the user's favorite color from the
     * Session.
     *
     * @param intent
     *            intent for the request
     * @return SpeechletResponse spoken and visual response for the intent
     */
//    private SpeechletResponse getColorFromSession(final Intent intent, final Session session) {
//        String speechText;
//        boolean isAskResponse = false;
//
//        // Get the user's favorite color from the session.
//        String favoriteColor = (String) session.getAttribute(LAST_OPTION_KEY);
//
//        // Check to make sure user's favorite color is set in the session.
//        if (StringUtils.isNotEmpty(favoriteColor)) {
//            speechText = String.format("Your favorite color is %s. Goodbye.", favoriteColor);
//        } else {
//            // Since the user's favorite color is not set render an error message.
//            speechText =
//                    "I'm not sure what your favorite color is. You can say, my favorite color is "
//                            + "red";
//            isAskResponse = true;
//        }
//
//        return getSpeechletResponse(speechText, speechText, isAskResponse);
//    }

    /**
     * Returns a Speechlet response for a speech and reprompt text.
     */
    private SpeechletResponse getSpeechletResponse(String speechText, String repromptText,
            boolean isAskResponse) {
        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("Energy & Utilities");
        card.setContent(speechText + ". See more here: http://www.energyandfacilities.harvard.edu/real-time-energy-production-demand");

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        if (isAskResponse) {
            // Create reprompt
            PlainTextOutputSpeech repromptSpeech = new PlainTextOutputSpeech();
            repromptSpeech.setText(repromptText);
            Reprompt reprompt = new Reprompt();
            reprompt.setOutputSpeech(repromptSpeech);

            return SpeechletResponse.newAskResponse(speech, reprompt, card);

        } else {
            return SpeechletResponse.newTellResponse(speech, card);
        }
    }
}
