package com.github.vssavin.umlib.domain.event;

import com.github.vssavin.umlib.base.controller.UmControllerBase;
import com.github.vssavin.umlib.config.LocaleConfig;
import com.github.vssavin.umlib.config.UmConfig;
import com.github.vssavin.umlib.data.pagination.Paged;
import com.github.vssavin.umlib.domain.language.MessageKey;
import com.github.vssavin.umlib.domain.language.UmLanguage;
import com.github.vssavin.umlib.domain.security.SecureService;
import com.github.vssavin.umlib.domain.user.UserSecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

/**
 * @author vssavin on 24.08.2023
 */
@RestController
@RequestMapping(EventController.EVENT_CONTROLLER_PATH)
public class EventController extends UmControllerBase {
    static final String EVENT_CONTROLLER_PATH = "/um/events";
    private static final String PAGE_EVENTS = "events";
    private static final String PAGE_LOGIN = UmConfig.LOGIN_URL.replace("/", "");

    private static final String EVENTS_ATTRIBUTE = "events";
    private static final String EVENTS_TYPES_ATTRIBUTE = "eventTypes";

    private static final Set<String> IGNORED_PARAMS = Collections.singleton("_csrf");

    private final SecureService secureService;
    private final UserSecurityService userSecurityService;
    private final EventService eventService;

    private final Set<String> pageLoginParams;
    private final Set<String> pageEventsParams;

    @Autowired
    public EventController(LocaleConfig localeConfig, UmLanguage language, UmConfig umConfig,
                           UserSecurityService userSecurityService, EventService eventService) {
        super(language, umConfig);
        this.secureService = umConfig.getSecureService();
        this.userSecurityService = userSecurityService;
        this.eventService = eventService;
        pageLoginParams = localeConfig.forPage(PAGE_LOGIN).getKeys();
        pageEventsParams = localeConfig.forPage(PAGE_EVENTS).getKeys();
    }

    @GetMapping
    public ModelAndView findEvents(
            final HttpServletRequest request, final HttpServletResponse response,
            @ModelAttribute final EventFilter eventFilter,
            @RequestParam(required = false, defaultValue = "1") final int page,
            @RequestParam(required = false, defaultValue = "5") final int size,
            @RequestParam(required = false) final String lang) {

        ModelAndView modelAndView = new ModelAndView(PAGE_EVENTS);
        if (userSecurityService.isAuthorizedAdmin(request)) {
            Paged<EventDto> events = eventService.findEvents(eventFilter, page, size);
            modelAndView.addObject(EVENTS_ATTRIBUTE, events);
            modelAndView.addObject(EVENTS_TYPES_ATTRIBUTE, Arrays.asList(EventType.values()));
        } else {
            modelAndView = getErrorModelAndView(UmConfig.LOGIN_URL,
                    MessageKey.ADMIN_AUTHENTICATION_REQUIRED_MESSAGE, lang);
            addObjectsToModelAndView(modelAndView, pageLoginParams, secureService.getEncryptMethodName(), lang);
            response.setStatus(403);
        }

        addObjectsToModelAndView(modelAndView, pageEventsParams, secureService.getEncryptMethodName(), lang);
        addObjectsToModelAndView(modelAndView, request.getParameterMap(), IGNORED_PARAMS);

        return modelAndView;
    }

}
