self.addEventListener('push',
    event => event.waitUntil(handlePushEvent(event))
);

function handlePushEvent(event) {

    let msg = 'This is a notification body';
    if (event.data) {
        msg = event.data.text();

        // If you send push messages in JSON format,
        // Here's how to get the data:
        //
        // const jsonData = event.data.json();
        // msg = jsonData['message'];
    }

    self.registration.showNotification('My Notification', {
        body: msg,
        tag: 'a tag'
    });
}
