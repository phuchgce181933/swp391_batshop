package com.ra.batshop;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.time.Duration;

public class SeleniumTest {

//    Test login

//    @Test
//    public void testLogin() {
//
//        WebDriverManager.chromedriver().setup();
//        WebDriver driver = new ChromeDriver();
//
//        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
//
//        driver.get("http://localhost:8080/login");
//
//        WebElement username = driver.findElement(By.name("email"));
//        WebElement password = driver.findElement(By.name("password"));
//
//        username.sendKeys("admin@gmail.com"); // đổi theo tài khoản trong DB
//        password.sendKeys("123456");
//
//        driver.findElement(By.cssSelector("button[type=submit]")).click();
//
//        driver.quit();
//    }

//    Test login sai

//    @Test
//    public void testLoginFail() {
//        WebDriverManager.chromedriver().setup();
//        WebDriver driver = new ChromeDriver();
//
//        driver.get("http://localhost:8080/login");
//
//        driver.findElement(By.name("email")).sendKeys("admin@gmail.com");
//        driver.findElement(By.name("password")).sendKeys("123");
//
//        driver.findElement(By.cssSelector("button[type=submit]")).click();
//    }

//    Test vào xem trang dashboard

//    @Test
//    public void testOpenDashboard() throws InterruptedException {
//
//        WebDriverManager.chromedriver().setup();
//        WebDriver driver = new ChromeDriver();
//
//        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
//
//        // mở login
//        driver.get("http://localhost:8080/login");
//
//        // login
//        driver.findElement(By.name("email")).sendKeys("minhquan15062004@gmail.com");
//        driver.findElement(By.name("password")).sendKeys("123");
//        driver.findElement(By.cssSelector("button[type=submit]")).click();
//
//        // vào dashboard
//        Thread.sleep(2000);
//        driver.get("http://localhost:8080/admin/dashboard");
//
//        Thread.sleep(8000);
//    }
}

