package com.gliesestudio.phantom.service.agant;

import java.util.Map;

public interface PhantomAgent {

    Map<String, ?> runWithContext(String input, Map<String, Object> context);

}
