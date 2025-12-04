# Remaining Fixes for Firebase Migration

## CRITICAL FIXES NEEDED (compile blockers):

### 1. EditProfileActivity.java - STUB IT OUT
Replace all DatabaseManager calls with TODO comments
Convert getOrganizationById/getVolunteerById to async callbacks

### 2. ProfileActivity.java - SAME AS ABOVE
Convert all sync AuthController calls to async

### 3. CreatePostActivity.java
Change: `boolean success = PostController.createPost(post);`
To: `PostController.createPost(post, callback);`

### 4. CreateRequestActivity.java  
- Fix List<Long> to List<String> for selectedOrganizationIds
- Convert createRequest to use callback
- Replace getFollowedOrganizationIds() with async version
- Replace getAllApprovedOrganizations() with SearchController.getAllOrganizations()

### 5. PostDetailActivity.java
- Convert getPostById to async
- Fix rating setVolunteerId/setOrganizationId (String not Long)
- Change RatingController.submitRating to use callback

### 6. OrganizationProfileActivity.java
- All similar to PostDetailActivity
- Convert isFollowing, followOrganization, unfollowOrganization to async

### 7. SearchActivity.java
- Fix searchOrganizations signature (remove minRating param)
- Add PostController.searchPosts() for post searching

### 8. NotificationsActivity.java
- Change userId type from long to String
- Convert all controller calls to async

### 9. RequestsActivity.java
- Add missing methods to VolunteerRequestController:
  - approveRequest()
  - rejectRequest()
  
### 10. NotificationAdapter.java
- Fix getIconForNotificationType to accept String instead of enum

### 11. Add missing controller methods
- DashboardActivity needs getUnreadCount in NotificationController

## STRATEGY:
Since there are too many files, I'll:
1. Fix the most critical compile errors first
2. Create stub implementations for complex activities
3. Add missing controller methods
4. Test basic flow (registration → login → dashboard)
