# trolley-tracker-agent
Android app for reporting the current location of the Greenville trolley.

## Dependencies
- Development
 - Android Studio (IDE)
 - Android API Level 19 (4.4.x KitKat)
- Running the App
 - Android KitKat or newer
 - Functional GPS Hardware
 - Google Play Services (Google Location Services)

## Functionality
This app uses Google Location Services API to request high-precision location updates. When it receives a location update, the service will issue a HTTP POST to the trolley-tracker-api endpoint.
