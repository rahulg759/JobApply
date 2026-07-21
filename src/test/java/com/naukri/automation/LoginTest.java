package com.naukri.automation;

import com.naukri.automation.pages.HomePage;
import com.naukri.automation.pages.LoginPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoginTest extends BaseTest {

    @Test
    @Tag("smoke")
    @DisplayName("Verify successful login to Naukri.com")
    void testSuccessfulLogin() {
        LoginPage loginPage = new LoginPage(page);
        HomePage homePage = loginPage.login(
                config.getEmail(),
                config.getPassword()
        );

        boolean loggedIn = homePage.isLoggedIn();
        log.info("Login verification result: {}", loggedIn);

        assertThat(loggedIn)
                .as("User should be logged in successfully")
                .isTrue();
    }
}
