/**
 * Copyright © 2016-2023 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thingsboard.server.dao.user;

import com.google.common.util.concurrent.ListenableFuture;
import org.thingsboard.server.common.data.User;
import org.thingsboard.server.common.data.id.CustomerId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.id.TenantProfileId;
import org.thingsboard.server.common.data.id.UserCredentialsId;
import org.thingsboard.server.common.data.id.UserId;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.page.PageLink;
import org.thingsboard.server.common.data.security.UserCredentials;
import org.thingsboard.server.dao.entity.EntityDaoService;

import java.util.List;

public interface UserService extends EntityDaoService {
	
	User findUserById(TenantId tenantId, UserId userId);

	ListenableFuture<User> findUserByIdAsync(TenantId tenantId, UserId userId);

	User findUserByEmail(TenantId tenantId, String email);

	User saveUser(User user, boolean doValidate);

    User findUserByTenantIdAndEmail(TenantId tenantId, String email);

	User saveUser(User user);

	UserCredentials findUserCredentialsByUserId(TenantId tenantId, UserId userId);
	
	UserCredentials findUserCredentialsByActivateToken(TenantId tenantId, String activateToken);

	UserCredentials findUserCredentialsByResetToken(TenantId tenantId, String resetToken);

	UserCredentials saveUserCredentials(TenantId tenantId, UserCredentials userCredentials);

	UserCredentials saveUserCredentials(TenantId tenantId, UserCredentials userCredentials, boolean doValidate);
	
	UserCredentials activateUserCredentials(TenantId tenantId, String activateToken, String password);
	
	UserCredentials requestPasswordReset(TenantId tenantId, String email);

    UserCredentials requestExpiredPasswordReset(TenantId tenantId, UserCredentialsId userCredentialsId);

    UserCredentials replaceUserCredentials(TenantId tenantId, UserCredentials userCredentials);

    void deleteUser(TenantId tenantId, UserId userId);

    PageData<User> findUsersByTenantId(TenantId tenantId, PageLink pageLink);

    PageData<User> findTenantAdmins(TenantId tenantId, PageLink pageLink);

    PageData<User> findSysAdmins(PageLink pageLink);

    PageData<User> findAllTenantAdmins(PageLink pageLink);

    PageData<User> findTenantAdminsByTenantsIds(List<TenantId> tenantsIds, PageLink pageLink);

    PageData<User> findTenantAdminsByTenantProfilesIds(List<TenantProfileId> tenantProfilesIds, PageLink pageLink);

    PageData<User> findAllUsers(PageLink pageLink);

    void deleteTenantAdmins(TenantId tenantId);

    PageData<User> findCustomerUsers(TenantId tenantId, CustomerId customerId, PageLink pageLink);

    PageData<User> findUsersByCustomerIds(TenantId tenantId, List<CustomerId> customerIds, PageLink pageLink);

    void deleteCustomerUsers(TenantId tenantId, CustomerId customerId);

    void setUserCredentialsEnabled(TenantId tenantId, UserId userId, boolean enabled);

    void resetFailedLoginAttempts(TenantId tenantId, UserId userId);

    int increaseFailedLoginAttempts(TenantId tenantId, UserId userId);

    void setLastLoginTs(TenantId tenantId, UserId userId);
}
