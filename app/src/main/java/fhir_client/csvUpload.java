package fhir_client;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import com.google.common.base.Charsets;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.hl7.fhir.r4.model.Observation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.r4.model.*;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.* ; 
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class csvUpload {

    private static final Logger ourLog = LoggerFactory.getLogger(csvUpload.class);

    public static void run() throws Exception {

        // Create a FHIR client
        FhirContext ctx = FhirContext.forR4();
        IParser parser = ctx.newJsonParser().setPrettyPrint(true);
        String serverBaseUrl = "http://10.0.0.50:10601/fhir";
        IGenericClient client = ctx.newRestfulGenericClient(serverBaseUrl);
        client.registerInterceptor(new LoggingInterceptor(false));

        // Open the CSV file for reading
        try (InputStream inputStream = new FileInputStream("app/src/main/resources/6_episode1_timeseries.csv")) {
            Reader reader = new InputStreamReader(inputStream, Charsets.UTF_8);

            CSVFormat format = CSVFormat.EXCEL.withHeader().withDelimiter(','); ; 
            CSVParser csvParser = format.parse(reader);

            // Loop throw each row in the CSV file
            for (CSVRecord nextRecord : csvParser.getRecords()) {

                // Sequence number - This could be used as an ID for generated resources
                String seqN = nextRecord.get("SEQN");

                // Add a log line - you can copy this to add more helpful logging
                ourLog.info("Processing row: {}", seqN);

                // Timestamp - This will be formatted in ISO8601 format
                String timestamp = nextRecord.get("TIME");
            
                // Patient ID
                String patientId = "1";

                // Patient Family Name
                String patientFamilyName = "Bar";

                // Patient Given Name
                String patientGivenName = "Foo";

                // Patient Gender - Values will be "M" or "F"
                String patientGender = "M";

                Patient patient = new Patient();
                patient.setId("Patient/" + patientId);
                patient.addName().setFamily(patientFamilyName).addGiven(patientGivenName);

                // Gender code needs to be mapped
                switch (patientGender) {
                case "M":
                    patient.setGender(Enumerations.AdministrativeGender.MALE);
                    break;
                case "F":
                    patient.setGender(Enumerations.AdministrativeGender.FEMALE);
                    break;
                }

                // Upload the patient resource using a client-assigned ID create
                client.update().resource(patient).execute();

                Patient patientRead = client.read().resource(Patient.class).withId(patientId).execute();
                System.out.println(parser.encodeResourceToString(patientRead));

                // http://loinc.org
                // code system https://loinc.org/fhir/loinc.xml
                
                // Diastolic blood pressure - This corresponds to LOINC code:
                // Code:        8462-4
                // Display:     Diastolic blood pressure
                // Unit System: http://unitsofmeasure.org
                // Unit Code:   mm[Hg]
                String dbp = nextRecord.get("DBP");

                if (dbp.length()>0){
                // Create the DBP Observation
                Observation dbpObservation = new Observation();
                dbpObservation.setId("Observation/dbp-" + seqN);
                dbpObservation.setStatus(Observation.ObservationStatus.FINAL);
                dbpObservation.setEffective(new DateTimeType(timestamp));
                Coding dbpCode = new Coding()
                    .setSystem("http://loinc.org")
                    .setCode("8462-4")
                    .setDisplay("Diastolic blood pressure");
                dbpObservation.getCode().addCoding(dbpCode);
                Quantity dbpValue = new SimpleQuantity()
                    .setSystem("http://unitsofmeasure.org")
                    .setUnit("mm[Hg]")
                    .setCode("mm[Hg]")
                    .setValue(Integer.parseInt(dbp));
                dbpObservation.setValue(dbpValue);
                dbpObservation.setSubject(new Reference("Patient/" + patientId));
                
                if (!dbp.equals("")) {client.update().resource(dbpObservation).execute();} 
                
                }
                // Systolic blood pressure - This corresponds to LOINC code:
                // Code:        8480-6
                // Display:     Systolic blood pressure
                // Unit System: http://unitsofmeasure.org
                // Unit Code:   mm[Hg]
                String sbp = nextRecord.get("SBP");
                
                if (sbp.length()>0){
                // Create the SBP Observation
                Observation sbpObservation = new Observation();
                sbpObservation.setId("Observation/sbp-" + seqN);
                sbpObservation.setStatus(Observation.ObservationStatus.FINAL);
                sbpObservation.setEffective(new DateTimeType(timestamp));
                Coding sbpCode = new Coding()
                    .setSystem("http://loinc.org")
                    .setCode("8480-6")
                    .setDisplay("Systolic blood pressure");
                sbpObservation.getCode().addCoding(sbpCode);
                Quantity sbpValue = new SimpleQuantity()
                    .setSystem("http://unitsofmeasure.org")
                    .setUnit("mm[Hg]")
                    .setCode("mm[Hg]")
                    .setValue(Integer.parseInt(sbp));
                sbpObservation.setValue(sbpValue);
                sbpObservation.setSubject(new Reference("Patient/" + patientId));

                if (!sbp.equals("")) {client.update().resource(sbpObservation).execute();} 
                
                }
                // Mean blood pressure
                // Code:        8478-0 
                // Display:     Mean blood pressure
                // Unit System: http://unitsofmeasure.org
                // Unit Code:   mm[Hg]
                String map = nextRecord.get("MAP");
                
                if(map.length()>0){
                // Create the MAP Observation
                Observation mapObservation = new Observation();
                mapObservation.setId("Observation/map-" + seqN);
                mapObservation.setStatus(Observation.ObservationStatus.FINAL);
                mapObservation.setEffective(new DateTimeType(timestamp));
                Coding mapCode = new Coding()
                    .setSystem("http://loinc.org")
                    .setCode("8478-0")
                    .setDisplay("Mean blood pressure");
                mapObservation.getCode().addCoding(mapCode);
                Quantity mapValue = new SimpleQuantity()
                    .setSystem("http://unitsofmeasure.org")
                    .setUnit("mm[Hg]")
                    .setCode("mm[Hg]")
                    .setValue(Float.parseFloat(map));
                mapObservation.setValue(mapValue);
                mapObservation.setSubject(new Reference("Patient/" + patientId));

                if (!map.equals("")) {client.update().resource(mapObservation).execute();} 
                
                }
                // Respiratory Rate
                // Code:        9279-1
                // Display:     Respiratory Rate
                // Unit System: http://unitsofmeasure.org
                // Unit Code:   /min
                String rr = nextRecord.get("RR");
                if (rr.length()>0){
                // Create the RR Observation
                Observation rrObservation = new Observation();
                rrObservation.setId("Observation/rr-" + seqN);
                rrObservation.setStatus(Observation.ObservationStatus.FINAL);
                rrObservation.setEffective(new DateTimeType(timestamp));
                Coding rrCode = new Coding()
                    .setSystem("http://loinc.org")
                    .setCode("9279-1")
                    .setDisplay("Respiratory Rate");
                rrObservation.getCode().addCoding(rrCode);
                Quantity rrValue = new SimpleQuantity()
                    .setSystem("http://unitsofmeasure.org")
                    .setUnit("/min")
                    .setCode("/min")
                    .setValue(Integer.parseInt(rr));
                rrObservation.setValue(rrValue);
                rrObservation.setSubject(new Reference("Patient/" + patientId));

                if (!rr.equals("")) {client.update().resource(rrObservation).execute();} 
                
                }
                // Body temperature
                // Code:        8310-5
                // Display:     Body temperature
                // Unit System: http://unitsofmeasure.org
                // Unit Code:   Cel
                String bt = nextRecord.get("BT");
                if (bt.length()>0){
                // Create the BT Observation
                Observation btObservation = new Observation();
                btObservation.setId("Observation/bt-" + seqN);
                btObservation.setStatus(Observation.ObservationStatus.FINAL);
                btObservation.setEffective(new DateTimeType(timestamp));
                Coding btCode = new Coding()
                    .setSystem("http://loinc.org")
                    .setCode("8310-5")
                    .setDisplay("Body temperature");
                btObservation.getCode().addCoding(btCode);
                Quantity btValue = new SimpleQuantity()
                    .setSystem("http://unitsofmeasure.org")
                    .setUnit("Cel")
                    .setCode("Cel")
                    .setValue(Float.parseFloat(bt));
                btObservation.setValue(btValue);
                btObservation.setSubject(new Reference("Patient/" + patientId));

                if (!bt.equals("")) {client.update().resource(btObservation).execute();} 
                
                }
                // Heart rate
                // Code:        8867-4
                // Display:     Heart rate
                // Unit System: http://unitsofmeasure.org
                // Unit Code:   /min
                String hr = nextRecord.get("HR");

                if (hr.length()>0){
                // Create the HR Observation
                Observation hrObservation = new Observation();
                hrObservation.setId("Observation/hr-" + seqN);
                hrObservation.setStatus(Observation.ObservationStatus.FINAL);
                hrObservation.setEffective(new DateTimeType(timestamp));
                Coding hrCode = new Coding()
                    .setSystem("http://loinc.org")
                    .setCode("8867-4")
                    .setDisplay("Heart rate");
                hrObservation.getCode().addCoding(hrCode);
                Quantity hrValue = new SimpleQuantity()
                    .setSystem("http://unitsofmeasure.org")
                    .setUnit("/min")
                    .setCode("/min")
                    .setValue(Integer.parseInt(hr));
                hrObservation.setValue(hrValue);
                hrObservation.setSubject(new Reference("Patient/" + patientId));
                if (!hr.equals("")) {client.update().resource(hrObservation).execute();} 
                
                }
                // Oxygen saturation in Arterial blood
                // Code:        2708-6
                // Display:     Oxygen saturation in Arterial blood
                // Unit System: http://unitsofmeasure.org
                // Unit Code:   %
                // Oxygen saturation in Arterial blood by Pulse oximetry
                // Code:        59408-5
                // Display:     Oxygen saturation in Arterial blood by Pulse oximetry
                // Unit System: http://unitsofmeasure.org
                // Unit Code:   %
                String osat = nextRecord.get("OSAT");
                
                if (osat.length()>0){
                // Create the OSAT Observation
                Observation osatObservation = new Observation();
                osatObservation.setId("Observation/osat-" + seqN);
                osatObservation.setStatus(Observation.ObservationStatus.FINAL);
                osatObservation.setEffective(new DateTimeType(timestamp));
                Coding osatCode = new Coding()
                    .setSystem("http://loinc.org")
                    .setCode("2708-6")
                    .setDisplay("Oxygen saturation in Arterial blood");
                Coding osatCode2 = new Coding()
                    .setSystem("http://loinc.org")
                    .setCode("59408-5")
                    .setDisplay("Oxygen saturation in Arterial blood by Pulse oximetry");
                osatObservation.getCode().addCoding(osatCode).addCoding(osatCode2);
                Quantity osatValue = new SimpleQuantity()
                    .setSystem("http://unitsofmeasure.org")
                    .setUnit("%")
                    .setCode("%")
                    .setValue(Integer.parseInt(osat));
                osatObservation.setValue(osatValue);
                osatObservation.setSubject(new Reference("Patient/" + patientId));
                if (!osat.equals("")) {client.update().resource(osatObservation).execute();} 
                
                }
                // Fasting glucose [Mass/volume] in Capillary blood
                // Code:        1556-0
                // Display:     Fasting glucose [Mass/volume] in Capillary blood
                // Unit System: http://unitsofmeasure.org
                // Unit Code:   mg/dL
                String glucose = nextRecord.get("GLUCOSE");

                if (glucose.length()>0){
                // Create the GLUCOSE Observation
                Observation glcObservation = new Observation();
                glcObservation.setId("Observation/glc-" + seqN);
                glcObservation.setStatus(Observation.ObservationStatus.FINAL);
                glcObservation.setEffective(new DateTimeType(timestamp));
                Coding glcCode = new Coding()
                    .setSystem("http://loinc.org")
                    .setCode("1556-0")
                    .setDisplay("Fasting glucose [Mass/volume] in Capillary blood");
                glcObservation.getCode().addCoding(glcCode);
                Quantity glcValue = new SimpleQuantity()
                    .setSystem("http://unitsofmeasure.org")
                    .setUnit("mg/dL")
                    .setCode("mg/dL")
                    .setValue(Integer.parseInt(glucose));
                glcObservation.setValue(glcValue);
                glcObservation.setSubject(new Reference("Patient/" + patientId));
                if (!glucose.equals("")) {client.update().resource(glcObservation).execute();} 
                }
                
                
            
            }
        }
    }

}