package com.ten.devs.cards.cards.user.infrastructure.db;

import com.ten.devs.cards.cards.user.domain.User;
import com.ten.devs.cards.cards.user.domain.UserId;
import com.ten.devs.cards.cards.user.domain.UserRepository;
import com.ten.devs.cards.cards.user.domain.UserSnapshot;
import com.ten.devs.cards.cards.user.domain.UserSpecification;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@AllArgsConstructor
class SqlDbUserRepository implements UserRepository {

  private final UserJpaRepository springDataRepository;

  private final UserMapper userMapper;

  @Override
  public void save(User user) {
    UserSnapshot userSnapshot = user.toSnapshot();
    springDataRepository.save(userMapper.mapToEntity(userSnapshot));
  }

    @Override
    public List<User> findAll() {
        return springDataRepository.findAll().stream()
                .map(userMapper::mapToSnapshot)
                .map(User::fromSnapshot)
                .toList();
    }

    @Override
    public List<User> findAllBySpecification(UserSpecification specification) {
        Specification<UserEntity> jpaSpec = UserSpecificationAdapter.toJpaSpecification(specification);
        return springDataRepository.findAll(jpaSpec).stream()
                .map(userMapper::mapToSnapshot)
                .map(User::fromSnapshot)
                .toList();
    }

    @Override
  public Optional<User> getUserByUsername(String username) {
    return springDataRepository.findByUsername(username)
      .map(userMapper::mapToSnapshot)
      .map(User::fromSnapshot);
  }

    @Override
  public User getUserById(UserId userId) {
    return User.fromSnapshot(userMapper.mapToSnapshot(springDataRepository.getReferenceById(userId.id())));
  }

}
