# trolley-tracker-agent  - [Archived in June 2023]

<p>From 2014-2021, Code For Greenville members built and maintained the technology which allowed thousands of locals and visitors to track the downtown Greenville trolleys in real-time from their mobile devices.<p>

<p>As of June 30, 2023, <a href="https://codeforamerica.org/news/reflections-on-the-brigade-networks-next-chapter/">Code For America officially withdrew fiscal sponsorship and use of the "Code For" trademark to <strong>all</strong> national brigades</a>, including Code For Greenville.</p>

<p>After July 1st, 2023, contributors can get involved with two re-branded efforts:</p>

<ul>
	<li>For ongoing civic projects, connect with <a href="https://opencollective.com/code-for-the-carolinas">Code For The Carolina</a> (which itself will rebrand by the end of 2023)</li>
	<li>For local tech APIs and OpenData projects, see the <a href="https://github.com/hackgvl">HackGreenville Labs repositories on GitHub</a> and connect with the team in the <em>#hg-labs</em> channel on the <a href="https://hackgreenville.com/join-slack">HackGreenville Slack</a></li>
</ul>

Android app previously used for reporting the current location of the Greenville, SC trolley.

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
