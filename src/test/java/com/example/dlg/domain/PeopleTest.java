package com.example.dlg.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.dlg.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class PeopleTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(People.class);
        People people1 = new People();
        people1.setId(1L);
        People people2 = new People();
        people2.setId(people1.getId());
        assertThat(people1).isEqualTo(people2);
        people2.setId(2L);
        assertThat(people1).isNotEqualTo(people2);
        people1.setId(null);
        assertThat(people1).isNotEqualTo(people2);
    }
}
