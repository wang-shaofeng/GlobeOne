/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package com.globe.inappupdate.remote_config

import com.globe.inappupdate.BuildConfig

val FORCED_UPGRADE_VAL = """
{
  "enabled": false,
  "conditions": {
    "ios": {
      "lowVersion": {
        "value": "1.0"
      },
      "highVersion": {
        "value": "1.1"
      }
    },
    "android": {
      "low_version": {
        "value": "1.0.0"
      },
      "high_version": {
        "value": "1.0.0"
      }
    }
  }
}
""".trimIndent()

val SMART_RATING_VAL = """
{
  "enabled": false,
  "conditions": {
    "ios": {
      "low_version": {
        "enable": false,
        "value": "2.3.0"
      },
      "high_version": {
        "enable": false,
        "value": "2.4.0"
      }
    },
    "android": {
      "low_version": {
        "enable": false,
        "value": "2.3.0"
      },
      "high_version": {
        "enable": false,
        "value": "2.4.0"
      }
    },
    "interval": {
      "enable": true,
      "value": 1296000
    },
    "onceeachversion": {
      "enable": false,
      "value": false
    },
    "enrolledaccounts": {
      "enable": true,
      "value": true
    },
    "firstshown": {
      "enable": true,
      "value": 86400
    },
    "waittime": {
      "enable": true,
      "value": 0
    },
    "optionID": {
      "enable": true,
      "value": 3
    },
    "omitted": {
      "enable": true,
      "value": true
    }
  },
  "options": {
    "en": [
      {
        "value": "Promos",
        "id": 1
      },
      {
        "value": "Rewards",
        "id": 2
      },
      {
        "value": "Profile & Accounts ",
        "id": 3
      },
      {
        "value": "App performance",
        "id": 4
      },
      {
        "value": "Overall experience",
        "id": 5
      },
      {
        "value": "Payments",
        "id": 6
      }
    ]
  }
}
""".trimIndent()

val RUSH_CAMPAIGN_VAL = """
    {
        "campaignLabel": "917-day",
        "isCampaignActive": false,
        "startDate": "2021-09-10T00:00:00.000",
        "endDate": "2021-09-19T23:59:59.000",
        "micrositeURLGame": "https://gsa.microsite.perxtech.io/game/36?token=[{user_token}]",
        "micrositeURLVoucher": "https://gsa.microsite.perxtech.io/wallet?token=[{user_token}]"
    }
""".trimIndent()

val DAC_VAL = """
{
    "expire_date":"2021-09-30T23:59:59.000"
}
""".trimIndent()

val BANNERS_VAL = """
    [
      {
        "campaignLabel": "917 Raffle",
        "title": "Win the prize of your choice!",
        "subtext": "Use your Rewards points to redeem raffle entries",
        "primaryCTA": "Join the Raffle",
        "primaryCTAType": "DEEPLINK",
        "primaryCTALink": "https://new.globe.com.ph/globeonesuperapp?action=reward_category_raffle_tab",
        "campaignSlot": 1,
        "startDate": "2021-08-10T00:00:00.000",
        "endDate": "2021-09-19T23:59:59.000",
        "bannerURL": "https://new.globe.com.ph/assets/files/media/gchance.png"
      },
      {
        "campaignLabel": "917 Flash Sale",
        "title": "The best deals come in flashes!",
        "subtext": "Use your Rewards points to redeem exciting flash sale offers",
        "primaryCTA": "View Offers",
        "primaryCTAType": "DEEPLINK",
        "primaryCTALink": "https://new.globe.com.ph/globeonesuperapp?action=reward_category_all_tab",
        "campaignSlot": 2,
        "startDate": "2021-08-10T00:00:00.000",
        "endDate": "2021-09-19T23:59:59.000",
        "bannerURL": "https://new.globe.com.ph/assets/files/media/gsupersale.png"
      },
      {
        "campaignLabel": "Spin the Wheel (RUSH)",
        "title": "Feeling lucky today?",
        "subtext": "Spin the wheel and get a chance to win exciting prizes",
        "primaryCTA": "Spin Now",
        "primaryCTAType": "DEEPLINK",
        "primaryCTALink": "https://new.globe.com.ph/globeonesuperapp?action=open_spinwheel",
        "campaignSlot": 3,
        "startDate": "2021-08-10T00:00:00.000",
        "endDate": "2021-09-19T23:59:59.000",
        "bannerURL": "https://new.globe.com.ph/assets/files/media/rushsoftlanding.png"
      },
      {
        "campaignLabel": "Event 1",
        "title": "Grow your business in the new normal",
        "subtext": "Learn from business leaders at the G Summit",
        "primaryCTA": "Register Now",
        "primaryCTAType": "EXTERNAL_LINK",
        "primaryCTALink": "http://glbe.co/GSummitReg",
        "campaignSlot": 4,
        "startDate": "2021-08-10T00:00:00.000",
        "endDate": "2021-09-18T23:59:59.000",
        "bannerURL": "https://new.globe.com.ph/assets/files/media/gsummit_0.png"
      },
      {
        "campaignLabel": "Event 2",
        "title": "It's a GG kind-of-day",
        "subtext": "Watch the G Legends Cup Finals and get a chance to win vouchers!",
        "primaryCTA": "View Event",
        "primaryCTAType": "EXTERNAL_LINK",
        "primaryCTALink": "http://glbe.co/0917GLegendsCup",
        "secondaryCTA": "View Details",
        "secondaryCTAType": "EXTERNAL_LINK",
        "secondaryCTALink": "http://glbe.co/0917GLegendsCupFAQs",
        "campaignSlot": 5,
        "startDate": "2021-08-18T00:00:00.000",
        "endDate": "2021-09-22T23:59:59.000",
        "bannerURL": "https://new.globe.com.ph/assets/files/media/glegends.png"
      },
      {
        "campaignLabel": "Claiming of free GBs",
        "title": "Let the feels take you away",
        "subtext": "Escape through music with world-class performances in the G Music Fest",
        "primaryCTA": "View Event",
        "primaryCTAType": "EXTERNAL_LINK",
        "primaryCTALink": "http://glbe.co/0917GMusicFest",
        "secondaryCTA": "Claim Free Data",
        "secondaryCTAType": "DEEPLINK",
        "secondaryCTALink": "https://new.globe.com.ph/globeonesuperapp?action=reward_landing",
        "campaignSlot": 6,
        "startDate": "2021-08-20T00:00:00.000",
        "endDate": "2021-09-26T23:59:59.000",
        "bannerURL": "https://new.globe.com.ph/assets/files/media/gmusicfest.png"
      }
    ]
""".trimIndent()

val FEATURE_ACTIVATION_VAL = """
{
  "dac": {
    "is_enabled": false
  },
  "campaigns_banner": {
    "is_enabled": false
  }
}
""".trimIndent()

val CHANNEL_MAP_VAL = """
{
  "CYO": "Create Your Own",
  "FB Operator Store (Formerly: NF ExtoCS - Transaction Processor)": "FB Operator Store",
  "Globe Portal (prod)": "Globe",
  "Globe Portal (dev)": "Globe",
  "Vanilla_NF_ORIGIN": "TM Easy Plan",
  "Connect": "Globe Hotline",
  "BB App": "Globe At Home",
  "GCASH": "GCash",
  "FBM": "FB Messenger",
  "SGBBAPP": "Globe At Home",
  "FB Operator Store": "FB Operator Store",
  "BB App origins": "Globe At Home",
  "GlobeOne": "GlobeOne",
  "My Globe App": "New GlobeOne",
  "Springfield": "Globe Rewards",
  "ORIGIN_SMS": "SMS/USSD",
  "ORIGIN_GYRO": "Retailer",
  "ORIGIN_GYRO_AMX": "Retailer",
  "ORIGIN_GYRO_SAL": "Share-a-Load"
}
""".trimIndent()

val remoteConfigDefaults = mapOf(
    BuildConfig.FORCED_UPDATE to FORCED_UPGRADE_VAL,
    BuildConfig.SMART_RATING_CONFIG to SMART_RATING_VAL,
    BuildConfig.RUSH_CAMPAIGN to RUSH_CAMPAIGN_VAL,
    BuildConfig.DAC_CONFIG to DAC_VAL,
    BuildConfig.BANNERS to BANNERS_VAL,
    BuildConfig.FEATURE_ACTIVATION to FEATURE_ACTIVATION_VAL,
    BuildConfig.CHANNEL_MAP to CHANNEL_MAP_VAL
)
