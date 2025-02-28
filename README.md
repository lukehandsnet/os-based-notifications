# OS-Based Notifications

A comprehensive Android application demonstrating various types of notifications using both local and Firebase Cloud Messaging (FCM) implementations.

## Overview

This application showcases different notification types and features available in Android, including:

- Basic notifications
- Expandable notifications with big text
- Notifications with action buttons
- Progress notifications
- High-priority/urgent notifications
- Grouped notifications
- Direct reply notifications

The app provides a simple UI to trigger these notifications locally and also includes a Firebase Cloud Messaging service to receive and display remote notifications.

## Features

### Local Notifications

The app demonstrates the following notification types that can be triggered directly from the UI:

1. **Simple Notification**: Basic notification with title and text
2. **Big Text Notification**: Expandable notification with longer content
3. **Action Notification**: Notification with action buttons
4. **Progress Notification**: Shows download progress with a progress bar
5. **Urgent Notification**: High-priority notification with vibration
6. **Grouped Notifications**: Multiple notifications grouped together
7. **Reply Notification**: Notification with direct reply capability

### Remote Notifications (FCM)

The app includes a Firebase Cloud Messaging service that can handle remote notifications with the same capabilities as local notifications. The FCM service can process different notification types based on the payload data:

- Simple notifications
- Big text notifications
- Action notifications
- Progress notifications
- Urgent notifications
- Reply notifications

## Technical Implementation

### Key Components

1. **MainActivity**: Handles UI interactions and local notification creation
2. **FCMService**: Processes incoming Firebase Cloud Messages and displays appropriate notifications

### Notification Channels

The app creates notification channels for Android 8.0 (API level 26) and higher, ensuring compatibility with modern Android versions.

### Permission Handling

The app implements runtime permission requests for the POST_NOTIFICATIONS permission on Android 13 (API level 33) and higher.

## Requirements

- Android SDK 23 or higher (Android 6.0 Marshmallow)
- Firebase project setup for FCM functionality

## Setup

1. Clone the repository
2. Open the project in Android Studio
3. For FCM functionality:
   - Create a Firebase project in the [Firebase Console](https://console.firebase.google.com/)
   - Add your Android app to the Firebase project
   - Download the `google-services.json` file and place it in the app directory
   - Follow Firebase setup instructions to complete the configuration

## Firebase Cloud Messaging

To send test notifications to the app via FCM, you can use the Firebase Console or the Firebase Admin SDK. Here's an example of a notification payload:

```json
{
  "to": "DEVICE_TOKEN",
  "notification": {
    "title": "Notification Title",
    "body": "Notification Body"
  },
  "data": {
    "type": "bigText",
    "big_text": "This is a much longer text that will be displayed when the notification is expanded."
  }
}
```

### Notification Types

Specify the notification type in the `data.type` field:

- `simple`: Basic notification
- `bigText`: Expandable notification with longer content
- `action`: Notification with action buttons
- `progress`: Progress notification (include `progress` field with a value from 0-100)
- `urgent`: High-priority notification
- `reply`: Notification with direct reply capability

## License

This project is open-source and available for educational purposes.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.