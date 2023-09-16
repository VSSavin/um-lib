package com.github.vssavin.umlib.domain.event;

import com.github.vssavin.umlib.AbstractTest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static com.github.vssavin.umlib.domain.event.EventController.EVENT_CONTROLLER_PATH;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author vssavin on 02.09.2023
 */
public class EventControllerTest extends AbstractTest {

	@Test
	public void shouldReturnForbiddenStatusFotNonAdminRole() throws Exception {
		MultiValueMap<String, String> registerParams = new LinkedMultiValueMap<>();

		registerParams.add("userId", "");

		ResultActions resultActions = mockMvc.perform(get(EVENT_CONTROLLER_PATH).params(registerParams)
			.with(getRequestPostProcessorForUser(testUser))
			.with(csrf()));

		resultActions.andExpect(status().is(HttpStatus.FORBIDDEN.value()));
	}

	@Test
	public void shouldLoggedInEventByUserId() throws Exception {
		MultiValueMap<String, String> registerParams = new LinkedMultiValueMap<>();
		String userId = "1";
		registerParams.add("userId", userId);

		ResultActions resultActions = mockMvc.perform(get(EVENT_CONTROLLER_PATH).params(registerParams)
			.with(getRequestPostProcessorForUser(testAdminUser))
			.with(csrf()));

		resultActions.andExpect(status().is(HttpStatus.OK.value()));

		String html = resultActions.andReturn().getResponse().getContentAsString();
		Document doc = Jsoup.parse(html);
		Element eventsTable = doc.getElementById("eventsTable");
		Elements trElements = eventsTable.getElementsByTag("tbody").first().getElementsByTag("tr");
		Element eventElement = trElements.get(0);
		String actualUserId = eventElement.getElementsByTag("td").get(1).text();
		EventType actualEventType = EventType.valueOf(eventElement.getElementsByTag("td").get(2).text());

		Assertions.assertEquals(userId, actualUserId);
		Assertions.assertEquals(EventType.LOGGED_IN, actualEventType);
	}

}
