package demo.backend.tests;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import java.util.HashMap;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import demo.backend.data.TestData;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

public class APITest {
	
	
	public Map<String, Object> test01_Map = new HashMap<String, Object>();
	public Map<String, Object> test02_Map = new HashMap<String, Object>();

	Response station01, station02;
	JsonPath station01JSON, station02JSON;
		
	
	@BeforeClass
	public void postData() {
		test01_Map.put("external_id", TestData.test01_externalId);
		test01_Map.put("name", TestData.test01_name);
		test01_Map.put("latitude", TestData.test01_latitude);
		test01_Map.put("longitude", TestData.test01_longitiude);
		test01_Map.put("altitude", TestData.test01_altitude);
		
		test02_Map.put("external_id", TestData.test02_externalId);
		test02_Map.put("name", TestData.test02_name);
		test02_Map.put("latitude", TestData.test02_latitude);
		test02_Map.put("longitude", TestData.test02_longitiude);
		test02_Map.put("altitude", TestData.test02_altitude);
		
	}
	
	
	/* Test method to register a weather station without an API key and validate the message body */
	
	@Test(priority = 1)
	public void validateErrorMessage() {
		RestAssured.baseURI = TestData.base_URI;
		RestAssured.basePath = TestData.base_Path;
		
		given()
			.contentType("application/json")
		.when()
			.post()
		.then()
			.statusCode(401)
			.body("cod", equalTo(401))
			.body("message", equalTo("Invalid API key. Please see http://openweathermap.org/faq#error401 for more info."));
			
	}
	
	
	/* Test method to register two stations and verifying the response code is 201 */
	
	@Test(priority = 2)
	public void registerNewStations() {
		RestAssured.baseURI = TestData.base_URI;
		RestAssured.basePath = TestData.base_Path;
		
		System.out.println("\n***** Registering First Station *****\n");
		station01 = given()
						.contentType("application/json")
						.queryParam("APPID", TestData.apiKey)
						.body(test01_Map)
					.when()
						.post()
					.then()
						.statusCode(201)
						.log().all()
						.and()
						.extract().response();
		
		station01JSON = station01.jsonPath();
		
		System.out.println("\n***** Registering Second Station*****\n");			
		
		 station02 = given()
						.contentType("application/json")
						.queryParam("APPID", TestData.apiKey)
						.body(test02_Map)
					.when()
						.post()
					.then()
						.statusCode(201)
						.log().all()
						.and()
						.extract().response();

		station02JSON = station02.jsonPath();					
				
	}
	
	
	/* Test method to verify Get call and the message body for the stations created */
	
	@Test(priority = 3)
	public void verifyStationRegistration() {
		RestAssured.baseURI = TestData.base_URI;
		RestAssured.basePath = TestData.base_Path;
		
		given()
			.contentType("application/json")
			.queryParam("APPID", TestData.apiKey)
		.when()
			.get()
		.then()
			.statusCode(200);
		
		//Validate the response values for station01
		Assert.assertEquals(station01JSON.get("external_id"), TestData.test01_externalId, "Station01 external_id value mismatch...");
		Assert.assertEquals(station01JSON.get("name"), TestData.test01_name,"Station01 name value mismatch...");
		Assert.assertEquals(station01JSON.getDouble("latitude") , TestData.test01_latitude, "Station01 latitude value mismatch...");
		Assert.assertEquals(station01JSON.getDouble("longitude"), TestData.test01_longitiude, "Station01 longitude value mismatch...");
		Assert.assertEquals(station01JSON.getInt("altitude"), TestData.test01_altitude, "Station01 altitude value mismatch...");
		
		//Validate the response values for station02 
		Assert.assertEquals(station02JSON.get("external_id"), TestData.test02_externalId, "Station02 external_id value mismatch...");
		Assert.assertEquals(station02JSON.get("name"), TestData.test02_name, "Station02 external_id value mismatch...");
		Assert.assertEquals(station02JSON.getDouble("latitude"), TestData.test02_latitude, "Station02 latitude value mismatch...");
		Assert.assertEquals(station02JSON.getDouble("longitude"), TestData.test02_longitiude, "Station02 longitude value mismatch...");
		Assert.assertEquals(station02JSON.getInt("altitude"), TestData.test02_altitude, "Station02 altitude value mismatch...");
	}
	
	
	/* Delete the created stations and verify that returned HTTP response is 204. */
	
	public void deleteStationWithID(String id) {
		RestAssured.baseURI = TestData.base_URI;
		RestAssured.basePath = TestData.base_Path +"/"+ id;
		
		given()
			.contentType("application/json")
			.queryParam("APPID", TestData.apiKey)
		.when()
			.delete()
		.then()
			.statusCode(204)
			.log().all();
		
	}
	
	
	/* verify that returned HTTP response is 404 and that message body contains “message”: “Station not found */
	
	public void deleteAlreadyDeletedStationWithID(String id) {
		RestAssured.baseURI = TestData.base_URI;
		RestAssured.basePath = TestData.base_Path +"/"+ id;
		
		Response delResponse = 
								given()
									.contentType("application/json")
									.queryParam("APPID", TestData.apiKey)
								.when()
									.delete()
								.then()
									.statusCode(404)
							//		.body("message", "Station not found")
									.extract().response();
								
			String jsonBody = delResponse.asString();
			Assert.assertEquals(jsonBody.contains("Station not found"), true);
	}
	
	
	/* Test method to verify Delete call. */
	
	@Test(priority = 4)
	public void deleteStations() {
		System.out.println("***** Deleting first station...*****\n");
		deleteStationWithID(station01JSON.getString("ID"));
		
		System.out.println("\n***** Deleting second station...*****\n");
		deleteStationWithID(station02JSON.getString("ID"));
		
		System.out.println("\nBoth the stations have been deleted successfully!\n");
	}
	
	
	/* Test method to verify that returned HTTP response is 404 and that message body contains “message”: “Station not found" for already stations*/
	
	@Test(priority = 5)
	public void valideErrorCodeForAlreadyDeletedStation() {
		System.out.println("***** Trying to delete an already deleted first station...*****\n");
		deleteAlreadyDeletedStationWithID(station01JSON.getString("ID"));
		
		System.out.println("***** Trying to delete an already deleted second station...*****\n");
		deleteAlreadyDeletedStationWithID(station02JSON.getString("ID"));
		
	}
	

}

