package com.example.demotddapp.model.dto;

import com.example.demotddapp.model.User;
import com.example.demotddapp.utils.annotation.ProfileImage;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
public class UserUpdateVM {

    @NotNull
    @Size(min = 4, max = 25)
    private String displayName;

    @ProfileImage
    private String image;

    public UserUpdateVM(User user) {
        this.displayName = user.getDisplayName();
    }
}
