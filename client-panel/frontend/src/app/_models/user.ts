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
  additionalInfo: any;         //Please review this property again with the backend
  address: string;
  city: string;
  clientId: string;
  clientUserId: string;
  communication: Communication;
  country: string;
  countryCode: string;
  creationDate: string;
  dob: string;
  email: string;
  fbId: string;
  firstName: string;
  gender: string;
  googleId: string;
  lastName: string;
  markTestUserProfile: boolean;
  mobile: string;
  undId: string;
}

export class Communication {
  email: string;
  mobile: string;
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

export class Identity {
  deviceId: string;
  sessionId: string;
  userId: string;
  clientId: string;
}


