package Student_Services.User;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class AccountControllerTest {
    private static final String tableName = "AccTestTable";

    @BeforeAll
    static void setUp() {
        // add table with dummy info
        SQLServerDataSource ds = new SQLServerDataSource();
        ds.setUser("scrummy@scrum-n-coke");
        ds.setPassword("qwdluief3qvwt4o!");
        ds.setServerName("scrum-n-coke.database.windows.net");
        ds.setPortNumber(Integer.parseInt("1433"));
        ds.setDatabaseName("scrum-n-coke-db");
        try (Connection con = ds.getConnection();
             Statement stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE)) {
            //remove any leftover table
            //stmt.executeUpdate("if object_id('" + tableName + "','U') is not null drop table " + tableName);
            stmt.executeUpdate("drop table if exists " + tableName);

            String sql = "create table " + tableName + " (username nvarchar(max) NOT NULL, user_password nvarchar(max) NOT NULL);";

            stmt.executeUpdate(sql);
            PreparedStatement addUserRow = con.prepareStatement("insert into " + tableName + " values(?, ?)");

            for (int i = 0; i < 5; i++) {
                addUserRow.setString(1, "test" + i + "@csbsju.edu");
                addUserRow.setString(2, "test" + i);
                addUserRow.addBatch();
            }
            addUserRow.executeBatch();
        }
        // Handle any errors that may have occurred.
        catch (SQLException e) {
            e.printStackTrace();
        }
        AccountController.setUserTable(tableName);
    }

    @AfterAll
    static void tearDown() {
        //remove table
        SQLServerDataSource ds = new SQLServerDataSource();
        ds.setUser("scrummy@scrum-n-coke");
        ds.setPassword("qwdluief3qvwt4o!");
        ds.setServerName("scrum-n-coke.database.windows.net");
        ds.setPortNumber(Integer.parseInt("1433"));
        ds.setDatabaseName("scrum-n-coke-db");
        try (Connection con = ds.getConnection();
             Statement stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE)) {
            String sql = "drop table if exists " + tableName;
            //remove  table
            stmt.execute(sql);
        }
        // Handle any errors that may have occurred.
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @ParameterizedTest(name="[{index}] username: {0} password: {1}")
    @CsvSource({
            "test0@csbsju.edu,test0",
            "test1@csbsju.edu,test1",
            "test2@csbsju.edu,test2",
            "test3@csbsju.edu,test3",
            "test4@csbsju.edu,test4"
    })
    void test_Login_User_Successful(String username, String pass) {
        assertTrue(AccountController.loginUser(username, pass));
    }

    @ParameterizedTest(name="[{index}] username: {0} password: {1}")
    @CsvSource({
            "test0@csbsju.edu,fail",
            "test1@csbsju.edu,''",
            "test2@csbsju.edu,failed",
            "test3@csbsju.edu,fa547il",
            "test4@csbsju.edu,fa654347536il"
    })
    void test_Login_User_Pass_Fail(String username, String pass) {
        assertFalse(AccountController.loginUser(username, pass));
    }

    @ParameterizedTest(name="[{index}] username: {0} password: {1}")
    @CsvSource({
            "fail1@csbsju.edu,test1",
            "2@csbsju.edu,test2",
            "@csbsju.edu,test3",
            "faidfgshdghsdfl4@csbsju.edu,test4"
    })
    void test_Login_User_Username_Fail(String username, String pass) {
        assertFalse(AccountController.loginUser(username, pass));
    }
    @ParameterizedTest(name="[{index}] username: {0} password: {1}")
    @CsvSource({
            "domainTest1@umn.edu,test1",
            "domainTest2@csbsju.edu@gmail.com,test2",
            "domainTest3,test3",
            "domainTest4@,test4",
            "domainTest6@csp.edu@csbsju.edu,test6"
    })
    void test_Login_User_Username_Domain_Fail(String username, String pass) {
        assertFalse(AccountController.loginUser(username, pass));
    }

    @ParameterizedTest(name="[{index}] username: {0} password: {1}")
    @CsvSource({
            "createTest1@csbsju.edu,test12345678",
            "createTest2@csbsju.edu,test22345690"
    })
    void test_Create_User_Successful(String username, String pass) {
        assertTrue(AccountController.createUser(username, pass));
    }

    @ParameterizedTest(name="[{index}] username: {0} password: {1}")
    @CsvSource({
            "createTest1@umn.edu,test123456789",
            "createTest2@gmail.com,test4567890",
            "createTest3,test423567890",
            "createTest4@,test9876543",
            "createTest5@yahoo.com,test5432643758"
    })
    void test_Create_User_Fail_Bad_Domain(String username, String pass) {
        assertFalse(AccountController.createUser(username, pass));
    }

    @ParameterizedTest(name="[{index}] username: {0} password: {1}")
    @CsvSource({
            "space test1,test42356",
            " spaceTest2,test63685",
            " spaceTest3 ,test574286",
            "spaceTest4 ,test634089"
    })
    void test_Create_User_Fail_Spaces(String username, String pass) {
        assertFalse(AccountController.createUser(username, pass));
    }

    @ParameterizedTest(name="[{index}] username: {0} password: {1}")
    @CsvSource({
            "semicolon;test1,test63425",
            ";semicolonTest2;,test85476",
            "semicolonTest4;,test3462"
    })
    void test_Create_User_Fail_Semicolon(String username, String pass) {
        assertFalse(AccountController.createUser(username, pass));
    }

    @ParameterizedTest(name="[{index}] pass: {0}")
    @ValueSource(strings = {
            "12345678",
            "qwertyuiop",
            "ghvarvnai;wo",
            "nusrte@vwaberytwve",
            "aaaaaaaa",
            "5432*^&12345678",
            "qwert^(#%yuiop",
            "nusrt|:{_*e@vwaberytwve",
            "aaaaaaaaaaaäaaa"
    })
    void test_Password_Checker_Good(String pass) {
        assertTrue(AccountController.passwordChecker(pass));
    }

    @ParameterizedTest(name="[{index}] pass: {0}")
    @ValueSource(strings = {
            "",
            "aaaaaaa",
            "1",
            "12",
            "123",
            "1234",
            "12345",
            "123456",
            "1234567"
    })
    void test_Password_Checker_Bad(String pass) {
        assertFalse(AccountController.passwordChecker(pass));
    }
}