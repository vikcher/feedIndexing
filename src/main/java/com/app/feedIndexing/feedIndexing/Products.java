package com.app.feedIndexing.feedIndexing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@Path("products")
public class Products {

	public String getResponse (String query, String maxRows) throws IOException
	{
		    String queryString = null;
		    if (!maxRows.equals(""))
		    {
		    	queryString = "http://localhost:8983/solr/products/select?q="+ query + "&wt=json&rows="+maxRows;
		    } else {
		    	queryString = "http://localhost:8983/solr/products/select?q="+ query + "&wt=json";
		    }
			URL url = new URL(queryString);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			
			if (conn.getResponseCode() != 200) {
				return "Invalid";
			}
			
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		    StringBuilder sb = new StringBuilder();
		    String output;
		    
		    while ((output = br.readLine()) != null)
		    {
		    	sb.append(output);
		    }
		    
		    return sb.toString();
	}
    
	/*
	 * Get the product list
	 */
    @GET
    @Produces("application/json")
    public String getProducts(@DefaultValue("*") @QueryParam("query") String query,
    		            	  @DefaultValue("") @QueryParam("maxRows") String maxRows) {
    	
    	String ret = null;
    	//Blanket JSON object to parse the string returned
    	JSONObject obj = null;
    	//JSON array holding all the product documents returned
    	JSONArray docs = null;
    	//JSON Object holding the response
    	JSONObject response = null;

        try {
			ret = getResponse(query,maxRows);
			if (ret.equals("Invalid"))
			{
				JSONObject error = new JSONObject();
				error.put("Type", "Error");
				return ret;
			}
			obj = new JSONObject(ret);
			response = obj.getJSONObject("response");
			docs = response.getJSONArray("docs");
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}

        /* 
         * The final list that is populated with product ID, name and description
         */
        JSONObject finalList = new JSONObject();
        JSONArray finalDocs = new JSONArray();
        try {
        	/*
        	 * For each object obtained in the docs array, create a smaller
        	 * JSON object with just the product name, ID and description and
        	 * add it to the final Docs array
        	 */
        	for (int i = 0; i < docs.length(); i++) {
        		JSONObject j = docs.getJSONObject(i);
        		JSONObject cleanProduct = new JSONObject();
        		cleanProduct.put("Product name", j.getString("product_name_s"));
        		cleanProduct.put("Product ID", j.getString("id"));
        		cleanProduct.put("Product description", j.getString("product_desc_t"));
        		finalDocs.put(cleanProduct);
        	}	
        } catch (JSONException e) {
        	e.printStackTrace();
        }

        /*
         * Create a final success object with type success and the documents array
         */
        try {
        	finalList.put("Type", "success");
        	finalList.put("numRows", docs.length());
			finalList.put("docs", finalDocs);
		} catch (JSONException e) {
			e.printStackTrace();
		}
    	return finalList.toString();
    }
}
