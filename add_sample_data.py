#!/usr/bin/env python3
"""
Script to add sample data to Firebase Realtime Database using REST API
Run this to populate your app with volunteers, organizations, and posts
"""

import requests
import time
import random
import json

# Firebase Realtime Database URL
FIREBASE_URL = "https://tounesna-85dfe-default-rtdb.firebaseio.com"

# Tunisian cities
cities = ["Tunis", "Sfax", "Sousse", "Kairouan", "Bizerte", "Gabès", "Ariana", "Gafsa", "Monastir", "Ben Arous"]

# Categories
categories = ["EDUCATION", "HEALTH", "ENVIRONMENT", "SOCIAL", "ANIMAL_WELFARE", "COMMUNITY"]

# Sample image URLs (using public images)
image_urls = [
    "https://images.unsplash.com/photo-1469571486292-0ba58a3f068b?w=800",
    "https://images.unsplash.com/photo-1488521787991-ed7bbaae773c?w=800",
    "https://images.unsplash.com/photo-1559027615-cd4628902d4a?w=800",
    "https://images.unsplash.com/photo-1532629345422-7515f3d16bb6?w=800",
    "https://images.unsplash.com/photo-1593113598332-cd288d649433?w=800",
    "https://i.imgur.com/HiHOhcB_d.webp?maxwidth=520&shape=thumb&fidelity=high",
]

def generate_push_id():
    """Generate a simple unique ID"""
    return f"-{int(time.time() * 1000)}{random.randint(1000, 9999)}"

def add_volunteers():
    """Add sample volunteers"""
    volunteer_data = [
        {"name": "Ahmed", "surname": "Ben Ali", "email": "ahmed@test.com", "phone": "+21612345601", "location": "Tunis", "skills": "Teaching, Community Work", "interests": "Education, Social"},
        {"name": "Fatma", "surname": "Trabelsi", "email": "fatma@test.com", "phone": "+21612345602", "location": "Sfax", "skills": "Healthcare, First Aid", "interests": "Health, Children"},
        {"name": "Mohamed", "surname": "Gharbi", "email": "mohamed@test.com", "phone": "+21612345603", "location": "Sousse", "skills": "Environmental Science", "interests": "Environment, Animals"},
        {"name": "Salma", "surname": "Mansour", "email": "salma@test.com", "phone": "+21612345604", "location": "Ariana", "skills": "Social Work, Psychology", "interests": "Social, Community"},
        {"name": "Youssef", "surname": "Kacem", "email": "youssef@test.com", "phone": "+21612345605", "location": "Monastir", "skills": "Teaching, Sports", "interests": "Education, Youth"},
    ]
    
    created_ids = []
    for vol_data in volunteer_data:
        vol_id = generate_push_id()
        time.sleep(0.1)  # Small delay to ensure unique IDs
        volunteer = {
            "id": vol_id,
            "name": vol_data["name"],
            "surname": vol_data["surname"],
            "email": vol_data["email"],
            "password": "test123",
            "phone": vol_data["phone"],
            "location": vol_data["location"],
            "profilePictureUrl": random.choice(image_urls),
            "interests": vol_data["interests"],
            "skills": vol_data["skills"],
            "availability": "Weekends, Evenings",
            "isApproved": True,
            "rating": round(random.uniform(3.5, 5.0), 1),
            "ratingCount": random.randint(5, 20),
            "createdAt": int(time.time() * 1000),
            "updatedAt": int(time.time() * 1000)
        }
        response = requests.put(f"{FIREBASE_URL}/volunteers/{vol_id}.json", json=volunteer)
        if response.status_code == 200:
            created_ids.append(vol_id)
            print(f"✅ Created volunteer: {vol_data['name']} {vol_data['surname']} (ID: {vol_id})")
        else:
            print(f"❌ Failed to create volunteer: {vol_data['name']}")
    
    return created_ids

def add_organizations():
    """Add sample organizations"""
    org_data = [
        {"name": "Tunisian Red Crescent", "domain": "HEALTH", "location": "Tunis", "website": "https://croissant-rouge.tn", "email": "contact@redcrescent.tn", "phone": "+21671234501"},
        {"name": "Green Tunisia", "domain": "ENVIRONMENT", "location": "Sfax", "website": "https://greentunisia.org", "email": "info@greentunisia.org", "phone": "+21674567801"},
        {"name": "Education For All", "domain": "EDUCATION", "location": "Sousse", "website": "https://eduforall.tn", "email": "contact@eduforall.tn", "phone": "+21673456701"},
        {"name": "Tunisian Animal Shelter", "domain": "ANIMAL_WELFARE", "location": "Ariana", "website": "https://animalcare.tn", "email": "help@animalcare.tn", "phone": "+21671345601"},
        {"name": "Community Builders", "domain": "COMMUNITY", "location": "Monastir", "website": "https://communitytn.org", "email": "contact@communitytn.org", "phone": "+21673234501"},
        {"name": "Hope Foundation", "domain": "SOCIAL", "location": "Bizerte", "website": "https://hope.tn", "email": "info@hope.tn", "phone": "+21672123401"},
    ]
    
    created_ids = []
    for org in org_data:
        org_id = generate_push_id()
        time.sleep(0.1)  # Small delay to ensure unique IDs
        organization = {
            "id": org_id,
            "name": org["name"],
            "domain": org["domain"],
            "location": org["location"],
            "website": org["website"],
            "email": org["email"],
            "password": "test123",
            "phone": org["phone"],
            "profilePictureUrl": random.choice(image_urls),
            "memberCount": random.randint(10, 100),
            "foundedYear": random.randint(2000, 2023),
            "isApproved": True,
            "rating": round(random.uniform(3.5, 5.0), 1),
            "ratingCount": random.randint(10, 50),
            "followersCount": random.randint(50, 500),
            "tags": f"{org['domain']}, Tunisia, Volunteer",
            "createdAt": int(time.time() * 1000),
            "updatedAt": int(time.time() * 1000)
        }
        response = requests.put(f"{FIREBASE_URL}/organizations/{org_id}.json", json=organization)
        if response.status_code == 200:
            created_ids.append(org_id)
            print(f"✅ Created organization: {org['name']} (ID: {org_id})")
        else:
            print(f"❌ Failed to create organization: {org['name']}")
    
    return created_ids

def add_posts(org_ids):
    """Add sample posts"""
    post_titles = [
        "Urgent: Medical Volunteers Needed",
        "Beach Cleanup Drive - Join Us!",
        "Teaching Children in Rural Areas",
        "Animal Shelter Needs Help",
        "Community Center Renovation",
        "Food Distribution for Families",
        "Tree Planting Campaign",
        "Free Medical Checkup Camp",
        "Sports Event for Youth",
        "Charity Marathon Registration",
        "Elderly Care Volunteers Wanted",
        "Clothing Drive for Winter",
        "Library Setup in School",
        "Blood Donation Camp",
        "Street Art for Awareness"
    ]
    
    post_descriptions = [
        "We urgently need volunteers to assist with our medical outreach program.",
        "Join us for a beach cleanup to protect our marine environment!",
        "Help us teach basic education to children in underserved communities.",
        "Our animal shelter needs volunteers for daily care and feeding.",
        "Help renovate our community center to serve more people.",
        "Assist in distributing food packages to families in need.",
        "Plant trees with us to fight climate change and beautify Tunisia.",
        "Medical professionals needed for free health checkup camp.",
        "Organize and supervise sports activities for underprivileged youth.",
        "Register now for our annual charity marathon event!",
        "Spend time with elderly people and brighten their day.",
        "Help collect and distribute warm clothing for winter season.",
        "Set up a community library in a local school.",
        "Volunteer as a blood donor and save lives.",
        "Create awareness through street art on social issues."
    ]
    
    for i in range(len(post_titles)):
        post_id = generate_push_id()
        time.sleep(0.1)  # Small delay to ensure unique IDs
        org_id = random.choice(org_ids)
        
        post = {
            "id": post_id,
            "organizationId": org_id,
            "title": post_titles[i],
            "description": post_descriptions[i],
            "category": random.choice(categories),
            "location": random.choice(cities),
            "imageUrl": random.choice(image_urls),
            "volunteersNeeded": random.randint(5, 30),
            "priority": random.choice(["HIGH", "MEDIUM", "LOW"]),
            "createdAt": int(time.time() * 1000) - random.randint(0, 30*24*60*60*1000),  # Random time in last 30 days
            "updatedAt": int(time.time() * 1000)
        }
        response = requests.put(f"{FIREBASE_URL}/posts/{post_id}.json", json=post)
        if response.status_code == 200:
            print(f"✅ Created post: {post_titles[i]} (ID: {post_id})")
        else:
            print(f"❌ Failed to create post: {post_titles[i]}")

def main():
    print("🚀 Starting to add sample data to Firebase...\n")
    
    print("📋 Adding Volunteers...")
    volunteer_ids = add_volunteers()
    print(f"\n✅ Added {len(volunteer_ids)} volunteers\n")
    
    print("🏢 Adding Organizations...")
    org_ids = add_organizations()
    print(f"\n✅ Added {len(org_ids)} organizations\n")
    
    print("📝 Adding Posts...")
    add_posts(org_ids)
    print(f"\n✅ Added 15 posts\n")
    
    print("🎉 Sample data added successfully!")
    print("\n📧 Login credentials for testing:")
    print("Volunteers: ahmed@test.com, fatma@test.com, mohamed@test.com (password: test123)")
    print("Organizations: contact@redcrescent.tn, info@greentunisia.org (password: test123)")

if __name__ == "__main__":
    main()
