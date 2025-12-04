#!/usr/bin/env python3
"""Test Firebase connectivity and add test accounts"""

import requests
import json

# Your Firebase Realtime Database URL (from google-services.json)
FIREBASE_URL = "https://tounesna-8021d-default-rtdb.firebaseio.com"

def test_read():
    """Test if we can read from Firebase"""
    print("Testing Firebase read access...")
    
    # Try to read volunteers
    response = requests.get(f"{FIREBASE_URL}/volunteers.json")
    print(f"Volunteers read status: {response.status_code}")
    if response.status_code == 200:
        data = response.json()
        if data:
            print(f"Found {len(data)} volunteers")
            for key, vol in list(data.items())[:3]:
                print(f"  - {vol.get('email', 'NO EMAIL')} ({vol.get('name', 'NO NAME')})")
        else:
            print("No volunteers found")
    else:
        print(f"Error: {response.text}")
    
    print()
    
    # Try to read organizations
    response = requests.get(f"{FIREBASE_URL}/organizations.json")
    print(f"Organizations read status: {response.status_code}")
    if response.status_code == 200:
        data = response.json()
        if data:
            print(f"Found {len(data)} organizations")
            for key, org in list(data.items())[:3]:
                print(f"  - {org.get('email', 'NO EMAIL')} ({org.get('name', 'NO NAME')})")
        else:
            print("No organizations found")
    else:
        print(f"Error: {response.text}")

def add_test_volunteer():
    """Add a test volunteer account"""
    print("\nAdding test volunteer account...")
    
    volunteer_id = "-TestVol123"
    volunteer_data = {
        "id": volunteer_id,
        "name": "Test",
        "surname": "Volunteer",
        "email": "test@test.com",
        "password": "test123",
        "phone": "12345678",
        "location": "Tunis",
        "profilePictureUrl": "",
        "interests": ["Education", "Health"],
        "skills": ["Teaching", "First Aid"],
        "availability": "Weekends",
        "isApproved": True,
        "rating": 0.0,
        "ratingCount": 0,
        "createdAt": 1732587000000,
        "updatedAt": 1732587000000
    }
    
    response = requests.put(
        f"{FIREBASE_URL}/volunteers/{volunteer_id}.json",
        json=volunteer_data
    )
    
    if response.status_code == 200:
        print(f"✅ Test volunteer added: test@test.com / test123")
    else:
        print(f"❌ Failed: {response.status_code} - {response.text}")

def add_test_organization():
    """Add a test organization account"""
    print("\nAdding test organization account...")
    
    org_id = "-TestOrg456"
    org_data = {
        "id": org_id,
        "name": "Test Organization",
        "domain": "Community Service",
        "location": "Tunis",
        "website": "https://test.org",
        "email": "org@test.com",
        "phone": "98765432",
        "password": "org123",
        "profilePictureUrl": "",
        "memberCount": 10,
        "foundedYear": 2020,
        "isApproved": True,
        "rating": 0.0,
        "ratingCount": 0,
        "followersCount": 0,
        "tags": ["Education", "Youth"],
        "createdAt": 1732587000000,
        "updatedAt": 1732587000000
    }
    
    response = requests.put(
        f"{FIREBASE_URL}/organizations/{org_id}.json",
        json=org_data
    )
    
    if response.status_code == 200:
        print(f"✅ Test organization added: org@test.com / org123")
    else:
        print(f"❌ Failed: {response.status_code} - {response.text}")

if __name__ == "__main__":
    print("=" * 50)
    print("Firebase Connection Test")
    print("=" * 50)
    
    test_read()
    
    print("\n" + "=" * 50)
    print("Adding Test Accounts")
    print("=" * 50)
    
    add_test_volunteer()
    add_test_organization()
    
    print("\n" + "=" * 50)
    print("Verifying...")
    print("=" * 50)
    test_read()
    
    print("\n✅ Done! Try logging in with:")
    print("   Volunteer: test@test.com / test123")
    print("   Organization: org@test.com / org123")
