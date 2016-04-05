package com.kentchiu.spring.base.web;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ResourceDocSnippetTest {

    @Test
    public void isMatches() throws Exception {
        ResourceDocSnippet snippet = new ResourceDocSnippet();
        assertThat(snippet.isMatches("http://127.0.0.1:8080/orders/3F078EE2-28D9-487F-9DF9-932047094FCB"), is(true));
        assertThat(snippet.isMatches("http://127.0.0.1:8080/orders/3F078EE2-28D9-487F-9DF9-932047094FCB/"), is(true));
        assertThat(snippet.isMatches("http://127.0.0.1:8080/orders/3F078EE2-28D9-487F-9DF9-932047094FCB?"), is(true));
        assertThat(snippet.isMatches("http://127.0.0.1:8080/orders/3F078EE2-28D9-487F-9DF9-932047094FCB?abc=def"), is(true));
        assertThat(snippet.isMatches("http://127.0.0.1:8080/orders/3F078EE2-28D9-487F-9DF9-932047094FCB/abc"), is(false));
    }

}