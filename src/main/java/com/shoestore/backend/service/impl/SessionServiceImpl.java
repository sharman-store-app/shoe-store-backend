package com.shoestore.backend.service.impl;

import com.shoestore.backend.dto.session.SessionRequestDto;
import com.shoestore.backend.model.Channel;
import com.shoestore.backend.model.DeviceType;
import com.shoestore.backend.model.Session;
import com.shoestore.backend.model.User;
import com.shoestore.backend.repository.SessionRepository;
import com.shoestore.backend.repository.UserRepository;
import com.shoestore.backend.service.SessionService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.net.URL;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;

    @Override
    @Transactional
    public void createSession(SessionRequestDto request, Authentication authentication,
                              HttpServletRequest httpServletRequest) {
        User user = null;
        if (authentication != null) {
            user = userRepository.findByEmail(authentication.getName()).orElseThrow(
                    () -> new EntityNotFoundException("User with email " + authentication.getName()
                            + "doesn't exist in database"));
        }

        String userAgent = httpServletRequest.getHeader("User-Agent");

        String browser = "Unknown";
        DeviceType deviceType = DeviceType.DESKTOP;
        if (userAgent != null) {
            if (userAgent.contains("Edg")
                    || userAgent.contains("EdgiOS")) {
                browser = "Edge";
            } else if (userAgent.contains("Chrome")
                    || userAgent.contains("CriOS")) {
                browser = "Chrome";
            } else if (userAgent.contains("Firefox")
                    || userAgent.contains("FxiOS")) {
                browser = "Firefox";
            } else if (userAgent.contains("Safari")) {
                browser = "Safari";
            }

            if (userAgent.contains("Tablet")) {
                deviceType = DeviceType.TABLET;
            } else if (userAgent.contains("iPad")) {
                deviceType = DeviceType.TABLET;
            } else if (userAgent.contains("Android")) {
                deviceType = DeviceType.MOBILE;
            } else if (userAgent.contains("iPhone")) {
                deviceType = DeviceType.MOBILE;
            }
        }

        Channel channel = Channel.DIRECT;
        String referrer = request.referrer();
        if (referrer != null && !referrer.isBlank()) {
            try {
                URL url = new URL(referrer);
                String host = url.getHost().toLowerCase();

                if (host.contains("google.") || host.contains("bing.")
                        || host.contains("duckduckgo.") || host.contains("yahoo.")) {
                    channel = Channel.ORGANIC;
                } else {
                    channel = Channel.REFERRAL;
                }
            } catch (MalformedURLException e) {
                channel = Channel.DIRECT;
            }
        }

        String country = "Unknown";

        Session session = new Session().setUser(user).setBrowser(browser)
                .setDeviceType(deviceType).setChannel(channel).setCountry(country);

        sessionRepository.save(session);
    }
}
