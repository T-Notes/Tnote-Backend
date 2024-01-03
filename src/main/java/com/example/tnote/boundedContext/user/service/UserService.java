package com.example.tnote.boundedContext.user.service;

import com.example.tnote.base.exception.UserErrorResult;
import com.example.tnote.base.exception.UserException;
import com.example.tnote.base.utils.CookieUtils;
import com.example.tnote.boundedContext.RefreshToken.repository.RefreshTokenRepository;
import com.example.tnote.boundedContext.user.dto.UserRequest;
import com.example.tnote.boundedContext.user.dto.UserResponse;
import com.example.tnote.boundedContext.user.entity.User;
import com.example.tnote.boundedContext.user.entity.auth.PrincipalDetails;
import com.example.tnote.boundedContext.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Transactional(readOnly = true)
@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    @Transactional
    public UserResponse signUp(String email, String name) {

        User user = User.builder()
                .email(email)
                .username(name)
                .build();

        return UserResponse.of(userRepository.save(user));
    }

    @Transactional
    public UserResponse updateExtraInfo(Long userId, UserRequest request) throws IOException {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorResult.USER_NOT_FOUND));

        log.info("과목, 학교, 경력, 알람 수신 여부 등록 및 수정");
        if(StringUtils.hasText(request.getSubject())) {
            user.updateSubject(request.getSubject());
        }
        if(StringUtils.hasText(request.getSchoolName())) {
            user.updateSchool(request.getSchoolName());
        }
        if(StringUtils.hasText(request.getGubun())) {
            user.updateSchoolGubun(request.getGubun());
        }
        if(StringUtils.hasText(request.getRegion())) {
            user.updateSchoolAddress(request.getRegion());
        }
        if(Objects.nonNull(request.getCareer())) {
            user.updateCareer(request.getCareer());
        }
        if(Objects.nonNull(request.isAlarm())) {
            user.updateAlarm(request.isAlarm());
        }

        return UserResponse.of(user);
    }

    @Transactional
    public void deleteUser(PrincipalDetails user) {

        Optional<User> currentUser = userRepository.findById(user.getId());

        if (currentUser.isEmpty()) {
            throw new UserException(UserErrorResult.USER_NOT_FOUND);
        }

        log.info("refresh token, user entity 삭제");
        refreshTokenRepository.deleteByKeyEmail(currentUser.get().getEmail());
        userRepository.delete(currentUser.get());
    }

    public void logout(HttpServletRequest request, HttpServletResponse response, PrincipalDetails user) {

        Optional<User> currentUser = userRepository.findById(user.getId());

        if (currentUser.isEmpty()) {
            throw new UserException(UserErrorResult.USER_NOT_FOUND);
        }

        CookieUtils.deleteCookie(request, response, "AccessToken");
    }

    /* URLConnection 을 전달받아 연결정보 설정 후 연결, 연결 후 수신한 InputStream 반환 */
    public InputStream getNetworkConnection(HttpURLConnection urlConnection) throws IOException {
        urlConnection.setConnectTimeout(3000);
        urlConnection.setReadTimeout(3000);
        urlConnection.setRequestMethod("GET");
        urlConnection.setDoInput(true);

        if(urlConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new IOException("HTTP error code : " + urlConnection.getResponseCode());
        }

        return urlConnection.getInputStream();
    }

    /* InputStream을 전달받아 문자열로 변환 후 반환 */
    public String readStreamToString(InputStream stream) throws IOException{
        StringBuilder result = new StringBuilder();

        BufferedReader br = new BufferedReader(new InputStreamReader(stream, "UTF-8"));

        String readLine;
        while((readLine = br.readLine()) != null) {
            result.append(readLine + "\n\r");
        }

        br.close();

        return result.toString();
    }

    public int findCityCode(String cityName) {
        Map<String, Integer> cityCodes = new HashMap<>();
        cityCodes.put("서울특별시", 100260);
        cityCodes.put("부산광역시", 100267);
        cityCodes.put("인천광역시", 100269);
        cityCodes.put("대전광역시", 100271);
        cityCodes.put("대구광역시", 100272);
        cityCodes.put("울산광역시", 100273);
        cityCodes.put("광주광역시", 100275);
        cityCodes.put("경기도", 100276);
        cityCodes.put("강원도", 100278);
        cityCodes.put("충청북도", 100280);
        cityCodes.put("충청남도", 100281);
        cityCodes.put("전라북도", 100282);
        cityCodes.put("전라남도", 100283);
        cityCodes.put("경상북도", 100285);
        cityCodes.put("경상남도", 100291);
        cityCodes.put("제주도", 100292);

        Integer cityCode = cityCodes.get(cityName);
        return cityCode;
    }
}
