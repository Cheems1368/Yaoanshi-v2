package com.medicine.service.ai;

import java.util.List;
import java.util.Map;

public interface AiUpstreamClient {

    String chat(List<Map<String, String>> messages);

    String providerHost();
}
