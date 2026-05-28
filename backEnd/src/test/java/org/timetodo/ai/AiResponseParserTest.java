package org.timetodo.ai;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AiResponseParserTest {

    private final AiResponseParser parser = new AiResponseParser();

    @Test
    void parseScheduleItemsWrapsSingleObjectContent() {
        String response = """
                {
                  "choices": [
                    {
                      "message": {
                        "content": "{\\"type\\":\\"task\\",\\"title\\":\\"report\\"}"
                      }
                    }
                  ]
                }
                """;

        List<Map<String, Object>> result = parser.parseScheduleItems(response);

        assertEquals(1, result.size());
        assertEquals("task", result.get(0).get("type"));
        assertEquals("report", result.get(0).get("title"));
    }

    @Test
    void parseScheduleItemsAcceptsTrailingCommaInContent() {
        String response = """
                {
                  "choices": [
                    {
                      "message": {
                        "content": "[{\\"type\\":\\"calendar\\",\\"title\\":\\"meeting\\",}]"
                      }
                    }
                  ]
                }
                """;

        List<Map<String, Object>> result = parser.parseScheduleItems(response);

        assertEquals(1, result.size());
        assertEquals("calendar", result.get(0).get("type"));
        assertEquals("meeting", result.get(0).get("title"));
    }
}
