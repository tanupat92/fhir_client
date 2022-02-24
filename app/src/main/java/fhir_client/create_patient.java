package fhir_client;
import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.r4.model.*;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.* ; 

import java.io.IOException;
public class create_patient {

    public create_patient() {

    }
    public static void run(){
        Patient pat = new Patient();
        HumanName name = pat.addName();
        name.setFamily("Boon").addGiven("Tanu").addGiven("P."); 
        
        FhirContext ctx = FhirContext.forR4();

        String serverBaseUrl = "http://10.0.0.50:10601/fhir";
        IGenericClient client = ctx.newRestfulGenericClient(serverBaseUrl);
        
        MethodOutcome outcome = client
            .create()
            .resource(pat)
            .execute(); 
        
        System.out.println(outcome.getId()); 

    }
}
