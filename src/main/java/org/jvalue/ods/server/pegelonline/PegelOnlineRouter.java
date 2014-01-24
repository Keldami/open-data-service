/*  Open Data Service
    Copyright (C) 2013  Tsysin Konstantin, Reischl Patrick

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
    
 */
package org.jvalue.ods.server.pegelonline;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ektorp.DbAccessException;
import org.jvalue.ods.data.pegelonline.PegelOnlineData;
import org.jvalue.ods.data.pegelonline.Station;
import org.jvalue.ods.db.DbAccessor;
import org.jvalue.ods.grabber.pegelonline.PegelOnlineAdapter;
import org.jvalue.ods.logger.Logging;
import org.jvalue.ods.server.Router;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The Class PegelOnlineRouter. defines routes that start with /pegelonline/
 * 
 */
public class PegelOnlineRouter implements Router {

	/** The routes. */
	private HashMap<String, Restlet> routes;

	private DbAccessor dbAccessor; 

	public PegelOnlineRouter(DbAccessor dbAccessor)
	{
		this.dbAccessor = dbAccessor;
	}
		

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jvalue.ods.adapter.RouterInterface#getRoutes()
	 */
	@Override
	public Map<String, Restlet> getRoutes() {
		routes = new HashMap<String, Restlet>();

		// gets data from all stations
		Restlet stationsRestlet = new Restlet() {
			@Override
			public void handle(Request request, Response response) {
				// Print the requested URI path
				String message = "";
				try {

					List<Station> sd = null;
					try {
						sd = getListOfStations(response);
					} catch (RuntimeException e) {
						return;
					}

					ObjectMapper mapper = new ObjectMapper();
					message += mapper.writeValueAsString(sd);

				} catch (IOException e) {
					String errorMessage = "Error during client request: " + e;
					Logging.error(this.getClass(), errorMessage);
					System.err.println(errorMessage);
				}

				if (!message.equals("")) {
					response.setEntity(message, MediaType.APPLICATION_JSON);
				} else {
					response.setEntity("No stations found.",
							MediaType.TEXT_PLAIN);
				}
			}

		};

		// gets the data of a single station
		Restlet singleStationRestlet = new Restlet() {
			@Override
			public void handle(Request request, Response response) {
				// Print the requested URI path
				String message = "";
				try {

					List<Station> sd = null;
					try {
						sd = getListOfStations(response);
					} catch (RuntimeException e) {
						String errorMessage = "Runtime exception occured: " + e;
						Logging.error(this.getClass(), errorMessage);
						return;
					}

					for (Station s : sd) {
						if (s.getLongname()
								.equalsIgnoreCase(
										(String) request.getAttributes().get(
												"station"))
								|| s.getShortname().equalsIgnoreCase(
										(String) request.getAttributes().get(
												"station"))) {

							ObjectMapper mapper = new ObjectMapper();
							message += mapper.writeValueAsString(s);

							break;
						}
					}

				} catch (IOException e) {
					String errorMessage = "Error during client request: " + e;
					Logging.error(this.getClass(), errorMessage);
					System.err.println(errorMessage);
				}
				if (!message.equals("")) {
					response.setEntity(message, MediaType.APPLICATION_JSON);
				} else {
					response.setEntity("Station not found.",
							MediaType.TEXT_PLAIN);
				}
			}
		};

		// gets the current measurement of a station including its current
		// height value
		Restlet currentMeasurementRestlet = new Restlet() {
			@Override
			public void handle(Request request, Response response) {
				// Print the requested URI path
				String message = "";
				try {
					List<Station> sd = null;
					try {
						sd = getListOfStations(response);
					} catch (RuntimeException e) {
						String errorMessage = "Runtime exception occured: " + e;
						Logging.error(this.getClass(), errorMessage);
						return;
					}

					for (Station s : sd) {
						if (s.getLongname()
								.equalsIgnoreCase(
										(String) request.getAttributes().get(
												"station"))
								|| s.getShortname().equalsIgnoreCase(
										(String) request.getAttributes().get(
												"station"))) {

							ObjectMapper mapper = new ObjectMapper();
							message += mapper.writeValueAsString(s
									.getTimeseries().get(0)
									.getCurrentMeasurement());
							break;
						}
					}

				} catch (IOException e) {
					String errorMessage = "Error during client request: " + e;
					Logging.error(this.getClass(), errorMessage);
					System.err.println(errorMessage);
				}
				if (!message.equals("")) {
					response.setEntity(message, MediaType.APPLICATION_JSON);
				} else {
					response.setEntity("Station not found.",
							MediaType.TEXT_PLAIN);
				}
			}
		};

		Restlet timeseriesRestlet = new Restlet() {
			@Override
			public void handle(Request request, Response response) {
				// Print the requested URI path
				String message = "";
				try {

					List<Station> sd = null;

					try {
						sd = getListOfStations(response);
					} catch (RuntimeException e) {
						String errorMessage = "Runtime exception occured: " + e;
						Logging.error(this.getClass(), errorMessage);
						return;
					}

					for (Station s : sd) {
						if (s.getLongname()
								.equalsIgnoreCase(
										(String) request.getAttributes().get(
												"station"))
								|| s.getShortname().equalsIgnoreCase(
										(String) request.getAttributes().get(
												"station"))) {

							ObjectMapper mapper = new ObjectMapper();
							message += mapper.writeValueAsString(s
									.getTimeseries());
							break;
						}
					}

				} catch (IOException e) {
					String errorMessage = "Error during client request: " + e;
					Logging.error(this.getClass(), errorMessage);
					System.err.println("Error during client request: " + e);
				}

				if (!message.equals("")) {
					response.setEntity(message, MediaType.APPLICATION_JSON);
				} else {
					response.setEntity("Station not found.",
							MediaType.TEXT_PLAIN);
				}
			}
		};

		// updates the pegelonline data or creates the initial document if
		// necessary
		Restlet updateRestlet = new Restlet() {

			@Override
			public void handle(Request request, Response response) {
				// Print the requested URI path
				String message = "";

				try {
					List<Station> list = new PegelOnlineAdapter()
							.getStationData();

					
					dbAccessor.connect();
					try {
						String last = dbAccessor.getLastDocumentId();
						PegelOnlineData pod = dbAccessor.getDocument(
								PegelOnlineData.class, last);
						pod.setStations(list);
						pod.getMetaData().setDate(
								new Timestamp(System.currentTimeMillis())
										.toString());
						dbAccessor.update(pod);
					} catch (DbAccessException e) {
						PegelOnlineData pod = new PegelOnlineData(list);
						dbAccessor.insert(pod);
					}

					message += "Database successfully updated.";
				} catch (IOException e) {
					System.err.println("Error during client request: " + e);
					message += "Could not update database: " + e.getMessage();
				}

				response.setEntity(message, MediaType.TEXT_PLAIN);

			}
		};

		routes.put("/pegelonline/stations", stationsRestlet);
		routes.put("/pegelonline/stations/{station}", singleStationRestlet);
		routes.put(
				"/pegelonline/stations/{station}/timeseries/currentMeasurement",
				currentMeasurementRestlet);
		routes.put("/pegelonline/stations/{station}/timeseries",
				timeseriesRestlet);
		routes.put("/pegelonline/update", updateRestlet);

		return routes;
	}

	// helper method to get the list of pegelonline stations
	/**
	 * Gets the list of stations from db.
	 *
	 * @param response the response
	 * @return the list of stations
	 * @throws RuntimeException the runtime exception
	 */
	private List<Station> getListOfStations(Response response)
			throws RuntimeException {
		List<Station> sd = null;

		try {	
			dbAccessor.connect();
			String docId = dbAccessor.getLastDocumentId();
			PegelOnlineData data = dbAccessor.getDocument(PegelOnlineData.class,
					docId);

			sd = data.getStations();

		} catch (RuntimeException e) {
			String errorMessage = "Could not retrieve data from db: " + e;
			Logging.error(this.getClass(), errorMessage);
			System.err.println(errorMessage);
			response.setEntity(
					"Could not retrieve data. Try to update database via /pegelonline/update.",
					MediaType.TEXT_PLAIN);
			throw e;
		}
		return sd;
	}

}