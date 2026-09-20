rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /{allPaths=**} {
      // শুধুমাত্র লগইন করা ইউজারদের আপলোড করার অনুমতি দিন
      allow read, write: if request.auth != null;
    }
  }
}