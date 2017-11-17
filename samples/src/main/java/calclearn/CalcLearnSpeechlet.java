/**
 * Copyright 2014-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with the License. A copy of the License is located at
 *
 * http://aws.amazon.com/apache2.0/
 *
 * or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package calclearn;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
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
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * This sample shows how to create a simple speechlet for handling intent
 * requests and managing session interactions.
 */
public class CalcLearnSpeechlet implements Speechlet {

	private static final Logger log = LoggerFactory.getLogger(CalcLearnSpeechlet.class);

	private static final String NAME_KEY = "NAME";
	private static final String NAME_SLOT = "Name";
	private static final String ANSWER_SLOT = "Answer";
	private static final String LAST_EXERCISE = "LastExercise";
	private static final String LAST_RESULT = "LastAnswer";
	private static final int MAX_RANDOM_INTEGER = 10;
	private static final List<String> CALC_OPERATORS = Arrays.asList("+", "-");

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
		//return getWelcomeResponse();
		return askNewExcercise(session);
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
		if ("MyNameIsIntent".equals(intentName)) {
			return setNameInSession(intent, session);
//		} else if ("WhatsMyColorIntent".equals(intentName)) {
//			return getNameFromSession(intent, session);
		} else if ("AnswerExerciseIntent".equals(intentName)) {
			return checkAnswer(intent, session);
		} else {
			//throw new SpeechletException("Invalid Intent");
			return askNewExcercise(session);
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
		String speechText
				= "Willkommen. Bitte verrate mir doch deinen Namen";
		// Ask again, when User doesn't answer
		String repromptText
				= "Ich warte auf deinen Namen, oder einen Namen deiner Wahl";

		return getSpeechletResponse(speechText, repromptText, true);
	}

	private SpeechletResponse askNewExcercise(final Session session) {
		String speechText, repromptText, excercise;
		final int rndNumberOne = new Random().nextInt(MAX_RANDOM_INTEGER);
		final int rndNumberTwo = new Random().nextInt(MAX_RANDOM_INTEGER);
		int result;
		final Random randomizer = new Random();
		final String rndOperator = CALC_OPERATORS.get(randomizer.nextInt(CALC_OPERATORS.size()));
		if (rndOperator.equals("-")) {
			if (rndNumberOne > rndNumberTwo) {
				result = rndNumberOne - rndNumberTwo;
				excercise = String.format("%s minus %s", rndNumberOne, rndNumberTwo);
			} else {
				result = rndNumberTwo - rndNumberOne;
				excercise = String.format("%s minus %s", rndNumberTwo, rndNumberOne);
			}
		} else {
			result = rndNumberOne + rndNumberTwo;
			excercise = String.format("%s plus %s", rndNumberOne, rndNumberTwo);
		}

		session.setAttribute(LAST_EXERCISE, excercise);
		session.setAttribute(LAST_RESULT, result);
		speechText = String.format("Wieviel ist %s", excercise);
		repromptText = String.format("Ich wiederhole noch einmal die Aufgabe: %s", speechText);
		return getSpeechletResponse(speechText, repromptText, true);
	}

	/**
	 * Creates a {@code SpeechletResponse} for the intent and stores the
	 * extracted color in the Session.
	 *
	 * @param intent intent for the request
	 * @return SpeechletResponse spoken and visual response the given intent
	 */
	private SpeechletResponse setNameInSession(final Intent intent, final Session session) {
		// Get the slots from the intent.
		Map<String, Slot> slots = intent.getSlots();

		// Get the color slot from the list of slots.
		Slot nameSlot = slots.get(NAME_SLOT);
		String speechText, repromptText;

		// Check for favorite color and create output to user.
		if (nameSlot != null) {
			// Store the user's favorite color in the Session and create response.
			String name = nameSlot.getValue();
			final String excercise = "100 plus 100";
			final int result = 200;
			session.setAttribute(NAME_KEY, name);
			session.setAttribute(LAST_EXERCISE, excercise);
			session.setAttribute(LAST_RESULT, result);
			speechText
					= String.format("%s, wieviel ist %s", name, excercise);
			repromptText
					= String.format("Bitte sage mir was %s ist, indem du sagst: die Antwort ist ", excercise);
		} else {
			// Render an error since we don't know what the users favorite color is.
			speechText = "Ich kenne deinen Namen leider nicht. Bitte sage deshalb, Mein Name ist Uwe.";
			repromptText
					= "Ich kenne deinen Namen leider nicht. Bitte sage deshalb, Mein Name ist Uwe.";
		}

		return getSpeechletResponse(speechText, repromptText, true);
	}

	/**
	 * Creates a {@code SpeechletResponse} for the intent and get the user's
	 * favorite color from the Session.
	 *
	 * @param intent intent for the request
	 * @return SpeechletResponse spoken and visual response for the intent
	 */
	private SpeechletResponse getNameFromSession(final Intent intent, final Session session) {
		String speechText;
		boolean isAskResponse = false;

		// Get the user's favorite color from the session.
		String favoriteColor = (String) session.getAttribute(NAME_KEY);

		// Check to make sure user's favorite color is set in the session.
		if (StringUtils.isNotEmpty(favoriteColor)) {
			speechText = String.format("Deine Name ist %s. Goodbye.", favoriteColor);
		} else {
			// Since the user's favorite color is not set render an error message.
			speechText
					= "I'm not sure what your favorite color is. You can say, my favorite color is "
					+ "red";
			isAskResponse = true;
		}

		return getSpeechletResponse(speechText, speechText, isAskResponse);
	}

	/**
	 * Creates a {@code SpeechletResponse} for the intent and stores the
	 * extracted color in the Session.
	 *
	 * @param intent intent for the request
	 * @return SpeechletResponse spoken and visual response the given intent
	 */
	private SpeechletResponse checkAnswer(final Intent intent, final Session session) {
		// Get the slots from the intent.
		Map<String, Slot> slots = intent.getSlots();

		// Get the color slot from the list of slots.
		Slot numberSlot = slots.get(ANSWER_SLOT);
		String speechText, repromptText;
		final int answer = Integer.valueOf(numberSlot.getValue());
		final int lastResult = (int) session.getAttribute(LAST_RESULT);
		final String lastExcercise = (String) session.getAttribute(LAST_EXERCISE);
		//ToDo: Integer.equals https://stackoverflow.com/questions/3637936/java-integer-equals-vs
		if (answer == lastResult) {
			speechText
					= String.format("Richtig. %s macht %s. Noch ein Spiel?", lastExcercise, answer);
		} else {
			speechText
					= String.format("Leider falsch. %s ist leider NICHT %s."
							+ "Die richtige Antwort ist %s", lastExcercise, answer, lastResult);
		}

		repromptText
				= String.format("Bitte sage mir was %s ist, indem du sagst: die Antwort ist ", lastExcercise);
		return getSpeechletResponse(speechText, repromptText, true);

	}

	/**
	 * Returns a Speechlet response for a speech and reprompt text.
	 */
	private SpeechletResponse getSpeechletResponse(String speechText, String repromptText,
			boolean isAskResponse) {
		// Create the Simple card content (for displaying devices only).
		SimpleCard card = new SimpleCard();
		card.setTitle("Session");
		card.setContent(speechText);

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
