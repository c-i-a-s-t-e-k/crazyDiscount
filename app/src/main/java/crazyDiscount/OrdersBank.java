package crazyDiscount;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OrdersBank {
    private JsonNode orders;

    public OrdersBank(String filePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        File file = new File(filePath);
        this.orders = objectMapper.readTree(file);
    }

    public JsonNode getOrders() {
        return orders;
    }
} 