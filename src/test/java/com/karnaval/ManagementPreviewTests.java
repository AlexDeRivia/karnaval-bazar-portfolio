package com.karnaval;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:karnaval-preview;DB_CLOSE_DELAY=-1")
@ActiveProfiles("demo")
@AutoConfigureMockMvc
class ManagementPreviewTests {
    @Autowired MockMvc mvc;

    @Test
    void publicPreviewContainsOnlyIllustrativeReadOnlyContent() throws Exception {
        mvc.perform(get("/gestion"))
                .andExpect(status().isOk())
                .andExpect(view().name("gestion/overview"))
                .andExpect(header().string("Cache-Control", "public, max-age=300"))
                .andExpect(content().string(containsString("datos ilustrativos")))
                .andExpect(content().string(not(containsString("<form"))));

        mvc.perform(get("/producto/index"))
                .andExpect(status().is3xxRedirection());
        mvc.perform(post("/gestion").with(csrf()))
                .andExpect(status().is3xxRedirection());
    }
}
