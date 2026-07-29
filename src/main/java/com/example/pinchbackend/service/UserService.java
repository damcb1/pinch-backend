package com.example.pinchbackend.service;

import com.example.pinchbackend.entity.User;
import com.example.pinchbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final FileUploadService fileUploadService;

    @Transactional
    public void deleteAccount(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));

        user.getRecipes().forEach(recipe -> fileUploadService.deleteByUrl(recipe.getImageUrl()));

        userRepository.delete(user);
    }
}