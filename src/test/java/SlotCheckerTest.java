import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class SlotCheckerTest {
    private WebDriver driver;
    private WebDriverWait wait;

    // private final String NO_APPOINTMENT_MSG = "No appointment slots are currently available. Please try another application centre if applicable";
    private final String APPOINTMENT_AVAILABLE_MSG = "Earliest Available Slot";
    private final String CENTER_CHENNAI = "Denmark Visa Application Centre, Chennai";
    private final String CENTER_HYDERABAD = "Denmark Visa Application Centre, Hyderabad";
    private final String VISA_SHORT_TERM = "Short Term";
    private final String VISA_VISITING = "Visting";

    private StringBuilder msgToUser = new StringBuilder();

    @BeforeClass(enabled = true)
    public void setUpRemote() throws Exception {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability("browserName", "chrome");
        capabilities.setCapability("enableVNC", true);
        capabilities.setCapability("enableVideo", true);
        driver = new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"), capabilities);
    }

    @BeforeClass(enabled = false)
    public void setUpLocal() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
    }

    @Test(enabled = false)
    public void sampleTest() throws IOException {
        try {
            driver.get("https://www.google.com");
            System.out.println("###########");
            System.out.println(driver.getTitle());
        } catch (Exception e) {
            System.out.println(e.fillInStackTrace());
        }
    }

    @Test(enabled = true)
    public void userLogin() throws Exception {
        try {
            driver.get(System.getProperty("app_url"));

            driver.manage().window().maximize();
            driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
            wait = new WebDriverWait(driver, 5);

 /*           WebElement consent = driver.findElement(By.id("onetrust-reject-all-handler"));
            wait.until(ExpectedConditions.elementToBeClickable(consent));
            consent.click();

            WebElement usernameTxt = driver.findElement(By.id("mat-input-0"));
            usernameTxt.sendKeys(System.getProperty("username"));

            WebElement passwordTxt = driver.findElement(By.id("mat-input-1"));
            passwordTxt.sendKeys(System.getProperty("password"));

            WebElement submitBtn = driver.findElement(By.cssSelector("button > .mat-button-wrapper"));
            submitBtn.click();

            wait.until(ExpectedConditions.titleContains("Dashboard"));

            waitForLoadingWindowToInvisible();

            WebElement newBookingBtn = driver.findElement(By.xpath("//div[@class='position-relative']/button[contains(.,'Start New Booking')]"));
            wait.until(ExpectedConditions.elementToBeClickable(newBookingBtn));
            Assert.assertTrue(driver.getTitle().contains("Dashboard"));

            newBookingBtn.click();

            waitForLoadingWindowToInvisible();
            WebElement center = driver.findElement(By.id("mat-select-value-1"));
            wait.until(ExpectedConditions.elementToBeClickable(center));

            matSelectInput("mat-select-0", CENTER_CHENNAI);

            matSelectInput("mat-select-4", VISA_SHORT_TERM);

            WebElement alertInfo = driver.findElement(By.cssSelector(".alert-info"));
            wait.until(ExpectedConditions.visibilityOf(alertInfo));

            scrollIntoAnElement(alertInfo);

            String msg = alertInfo.getText();
            System.out.println("ACTUAL MSG FOR SHORT TERM: " + msg);

            createMessageForUser(msg, CENTER_CHENNAI, VISA_SHORT_TERM);

            matSelectInput("mat-select-4", VISA_VISITING);

            WebElement alertInfo1 = driver.findElement(By.cssSelector(".alert-info"));
            wait.until(ExpectedConditions.visibilityOf(alertInfo1));
            scrollIntoAnElement(alertInfo1);

            String msg1 = alertInfo1.getText();

            System.out.println("ACTUAL MSG FOR VISITING: " + msg1);

            createMessageForUser(msg1, CENTER_CHENNAI, "Visiting Family and Friends");

            matSelectInput("mat-select-0", CENTER_HYDERABAD);

            matSelectInput("mat-select-4", VISA_VISITING);

            WebElement alertInfo2 = driver.findElement(By.cssSelector(".alert-info"));
            wait.until(ExpectedConditions.visibilityOf(alertInfo2));
            scrollIntoAnElement(alertInfo2);

            String msg2 = alertInfo2.getText();

            System.out.println("ACTUAL MSG FOR VISITING: " + msg2);

            createMessageForUser(msg2, CENTER_HYDERABAD, "Visiting Family and Friends");
            System.out.println(msgToUser.toString());
*/
            if (msgToUser.toString().length() > 0) {
                Assert.fail("No slots");
            } else {
                Assert.assertTrue(true);
            }
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
        }
    }

    private void createMessageForUser(String msg, String centerName, String visaType) {
        if (msg.contains(APPOINTMENT_AVAILABLE_MSG)) {
            msgToUser.append(msg);
            msgToUser.append(" at ");
            msgToUser.append(centerName);
            msgToUser.append(" for visa type:");
            msgToUser.append(visaType);
            msgToUser.append(System.lineSeparator());
        }
    }

    private void scrollIntoAnElement(WebElement alertInfo2) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", alertInfo2);
    }

    private void waitForLoadingWindowToInvisible() {
        try {
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//*[contains(@class,'loading-foreground')]")));
        } catch (Exception e) {
        }
    }

    public void matSelectInput(String id, String value) {

        WebElement elem = driver.findElement(By.id(id));
        wait.until(ExpectedConditions.elementToBeClickable(elem));
        scrollIntoAnElement(elem);
        driver.findElement(By.id(id)).click();
        driver.findElement(By.xpath("//span[@class='mat-option-text' and contains(.,'" + value + "')]")).click();
        waitForLoadingWindowToInvisible();
    }

    private static void takeSnapShot(WebDriver webdriver, String fileWithPath) throws Exception {
        TakesScreenshot scrShot = ((TakesScreenshot) webdriver);
        File SrcFile = scrShot.getScreenshotAs(OutputType.FILE);
        File DestFile = new File(fileWithPath);
        FileUtils.copyFile(SrcFile, DestFile);
    }

    @AfterClass
    public void tearDown() {
        driver.quit();
    }
}
