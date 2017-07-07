package io.solwind;

import java.io.Serializable;

/**
 * Created by theso on 6/19/2017.
 */
public class CustomDto implements Serializable {
    final String str;
    final Integer intg;

    public CustomDto(String str, Integer intg) {
        this.str = str;
        this.intg = intg;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CustomDto customDto = (CustomDto) o;

        if (str != null ? !str.equals(customDto.str) : customDto.str != null) return false;
        return intg != null ? intg.equals(customDto.intg) : customDto.intg == null;
    }

    @Override
    public int hashCode() {
        int result = str != null ? str.hashCode() : 0;
        result = 31 * result + (intg != null ? intg.hashCode() : 0);
        return result;
    }
}
