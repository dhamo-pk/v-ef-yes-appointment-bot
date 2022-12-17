import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.commons.io.FileUtils;
import org.bouncycastle.crypto.agreement.jpake.JPAKEPrimeOrderGroup;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class SlotCheckerTest {
    private WebDriver driver;
    private WebDriverWait wait;
 private Wait<WebDriver> gWait;
    // private final String NO_APPOINTMENT_MSG = "No appointment slots are currently available. Please try another application centre if applicable";
    private final String APPOINTMENT_AVAILABLE_MSG = "Earliest Available Slot";
    private final String CENTER_CHENNAI = "Denmark Visa Application Centre, Chennai";
    private final String CENTER_HYDERABAD = "Denmark Visa Application Centre, Hyderabad";
    private final String CENTER_BANGALORE = "Denmark Visa Application Centre, Bangalore";

    private final String VISA_SHORT_TERM = "Short Term";
    private final String VISA_VISITING = "Visting";

    private StringBuilder msgToUser = new StringBuilder();

    private String tempCenter = "";

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

    private void initialLaunchSetUp() {
        driver.get(System.getProperty("app_url"));
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        wait = new WebDriverWait(driver, 5);
        gWait = new FluentWait<WebDriver>(driver).withTimeout(Duration.ofSeconds(100))
                .pollingEvery(Duration.ofMillis(15000)).ignoring(NoSuchElementException.class);

    }

    private void waitForElementToBeClickable(WebElement elem) {
        gWait.until(ExpectedConditions.elementToBeClickable(elem));
    }

    private void performLogin() {
        WebElement consent = driver.findElement(By.id("onetrust-reject-all-handler"));
        waitForElementToBeClickable(consent);
        consent.click();

        WebElement usernameTxt = driver.findElement(By.id("mat-input-0"));
        usernameTxt.sendKeys(System.getProperty("username"));

        WebElement passwordTxt = driver.findElement(By.id("mat-input-1"));
        passwordTxt.sendKeys(System.getProperty("password"));

        WebElement submitBtn = driver.findElement(By.cssSelector("button > .mat-button-wrapper"));
        submitBtn.click();


        WebElement element = driver.findElement(By.tagName("header"));

        JavascriptExecutor js = (JavascriptExecutor)driver;
        js.executeScript("arguments[0].scrollIntoView();", element);

        //gWait.until(ExpectedConditions.titleContains("Dashboard"));

        waitForLoadingWindowToInvisible();
    }

    private void searchForAnyVisaCategoryInAnyCenter(String centerName, String typeOfVisa) {

        if (!tempCenter.contentEquals(centerName)) {
            matSelectInput("mat-select-0", centerName);
        }

        tempCenter = centerName;

        matSelectInput("mat-select-4", typeOfVisa);

        wait.until(ExpectedConditions.visibilityOf(driver.findElement(By.cssSelector(".alert-info"))));

        scrollIntoAnElement(driver.findElement(By.cssSelector(".alert-info")));

        String msg = driver.findElement(By.cssSelector(".alert-info")).getText();

        System.out.println(String.format("STATUS : Center: %s., Type: %s., Msg: %s", centerName, typeOfVisa, msg));

        createMessageForUser(msg, centerName, typeOfVisa);

        takeSnapShot(driver, "target/" + centerName + "_" + typeOfVisa + ".png");
    }

    @Test(enabled = true)
    public void userLogin() throws Exception {
        try {
            this.initialLaunchSetUp();

            this.performLogin();

            this.navigateToBookingSection();

            this.searchForAnyVisaCategoryInAnyCenter(CENTER_CHENNAI, VISA_SHORT_TERM);

            this.searchForAnyVisaCategoryInAnyCenter(CENTER_CHENNAI, VISA_VISITING);

            this.searchForAnyVisaCategoryInAnyCenter(CENTER_HYDERABAD, VISA_VISITING);

            if (msgToUser.toString().length() > 0) {
                Assert.assertTrue(true);
                System.out.println("#####" + msgToUser.toString());
            } else {
                Assert.fail("No slots");
            }
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
            takeSnapShot(driver, "target/Exception.png");
            System.out.println("Page Source: "+ driver.findElement(By.xpath("//html/body")).getText());
            Assert.fail("Exception:" + e.getMessage());
        }
    }

    private void navigateToBookingSection() {
        WebElement newBookingBtn = driver.findElement(By.xpath("//div[@class='position-relative']/button[contains(.,'Start New Booking')]"));
        waitForElementToBeClickable(newBookingBtn);
        Assert.assertTrue(driver.getTitle().contains("Dashboard"));

        newBookingBtn.click();

        waitForLoadingWindowToInvisible();

        WebElement center = driver.findElement(By.id("mat-select-value-1"));
        waitForElementToBeClickable(center);
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
            gWait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//*[contains(@class,'loading-foreground')]")));
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

    private static void takeSnapShot(WebDriver webdriver, String fileWithPath) {
        try {
            TakesScreenshot scrShot = ((TakesScreenshot) webdriver);
            File SrcFile = scrShot.getScreenshotAs(OutputType.FILE);
            File DestFile = new File(fileWithPath);
            FileUtils.copyFile(SrcFile, DestFile);
        } catch (IOException e) {
            System.out.println(e.getLocalizedMessage());
        }
    }

    @AfterClass
    public void tearDown() {
        driver.quit();
    }
}
