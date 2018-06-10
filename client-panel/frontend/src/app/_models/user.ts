export class User {
  username: string;
  password: string;
  firstName: string;
  lastName: string;
}


export class SocialId {
  fbId: string;
  googleId: string;
  mobile: string;
  email: string;
}

export class StandardInfo {
  firstName: string;
  lastName: string;
  gender: string;
  dob: string;
  country: string;
  countryCode: string;
}

export class UserParams {
  public static params = [
    "{user.socialId.email}",
    "{user.socialId.mobile}",
    "{user.standardInfo.firstName}",
    "{user.standardInfo.lastName}",
    "{user.standardInfo.gender}",
    "{user.standardInfo.dob}",
    "{user.standardInfo.country}",
    "{user.standardInfo.countryCode}"
  ];
}

export class EventUser {
  identity: Identity;
  email: string;
  clientUserId: string;
  undId: string;
  fbId: string;
  googleId: string;
  mobile: string;
  firstName: string;
  lastName: string;
  gender: string;
  dob: string;
  country: string;
  countryCode: string;
  clientId: string;
  additionalInfo: string;         //Please review this property again with the backend
  creationDate: string;
}

export class Identity {
  deviceId: string;
  sessionId: string;
  userId: string;
  clientId: string;
}
export class Event {
  name: string;
  identity: Identity;
  creationTime: string;
  ipAddress: string;
  city: string;
  state: string;
  country: string;
  latitude: string;
  longitude: string;
  agentString: string;
  userIdentified: boolean;
  // lineitem,attributes,startDate,EndDate to be added.
}
