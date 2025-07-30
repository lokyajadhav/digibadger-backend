package com.taashee.badger.serviceimpl;

import com.taashee.badger.models.User;
import com.taashee.badger.repositories.UserRepository;
import com.taashee.badger.repositories.UserInvitationRepository;
import com.taashee.badger.repositories.OrganizationUserRepository;
import com.taashee.badger.repositories.OrganizationStaffRepository;
import com.taashee.badger.repositories.BadgeInstanceRepository;
import com.taashee.badger.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserInvitationRepository userInvitationRepository;
    
    @Autowired
    private OrganizationUserRepository organizationUserRepository;
    
    @Autowired
    private OrganizationStaffRepository organizationStaffRepository;
    
    @Autowired
    private BadgeInstanceRepository badgeInstanceRepository;

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    @Override
    public void saveUser(User user) {
        userRepository.save(user);
    }

    @Override
    public java.util.List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        // First delete user invitations
        userInvitationRepository.deleteByUserId(userId);
        // Then delete organization users
        organizationUserRepository.deleteByUserId(userId);
        // Then delete organization staff
        organizationStaffRepository.deleteByUserId(userId);
        // Then delete badge instances where user is recipient
        badgeInstanceRepository.deleteByRecipientId(userId);
        // Finally delete the user
        userRepository.deleteById(userId);
    }
} 