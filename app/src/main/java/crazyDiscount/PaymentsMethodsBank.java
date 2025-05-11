package crazyDiscount;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PaymentsMethodsBank {
    private JsonNode paymentMethods;

    public PaymentsMethodsBank(String filePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        File file = new File(filePath);
        this.paymentMethods = objectMapper.readTree(file);
    }

    public JsonNode getPaymentMethods() {
        return paymentMethods;
    }
} 