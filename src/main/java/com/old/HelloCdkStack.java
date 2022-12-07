package com.old;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.CfnTag;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Fn;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.backup.CfnBackupPlan;
import software.amazon.awscdk.services.backup.CfnBackupPlan.BackupPlanResourceTypeProperty;
import software.amazon.awscdk.services.backup.CfnBackupPlan.BackupRuleResourceTypeProperty;
import software.amazon.awscdk.services.backup.CfnBackupPlan.CopyActionResourceTypeProperty;
import software.amazon.awscdk.services.backup.CfnBackupPlan.LifecycleResourceTypeProperty;
import software.amazon.awscdk.services.backup.CfnBackupSelection;
import software.amazon.awscdk.services.backup.CfnBackupSelection.BackupSelectionResourceTypeProperty;
import software.amazon.awscdk.services.backup.CfnBackupSelection.ConditionResourceTypeProperty;
import software.amazon.awscdk.services.backup.CfnBackupVault;
import software.amazon.awscdk.services.cloudfront.CfnDistribution;
import software.amazon.awscdk.services.cloudfront.CfnDistribution.CacheBehaviorProperty;
import software.amazon.awscdk.services.cloudfront.CfnDistribution.CookiesProperty;
import software.amazon.awscdk.services.cloudfront.CfnDistribution.CustomOriginConfigProperty;
import software.amazon.awscdk.services.cloudfront.CfnDistribution.DefaultCacheBehaviorProperty;
import software.amazon.awscdk.services.cloudfront.CfnDistribution.DistributionConfigProperty;
import software.amazon.awscdk.services.cloudfront.CfnDistribution.ForwardedValuesProperty;
import software.amazon.awscdk.services.cloudfront.CfnDistribution.OriginProperty;
import software.amazon.awscdk.services.cloudwatch.Alarm;
import software.amazon.awscdk.services.cloudwatch.AlarmActionConfig;
import software.amazon.awscdk.services.cloudwatch.AlarmProps;
import software.amazon.awscdk.services.cloudwatch.CfnAlarm;
import software.amazon.awscdk.services.cloudwatch.CfnAlarm.DimensionProperty;
import software.amazon.awscdk.services.cloudwatch.CfnAlarm.MetricDataQueryProperty;
import software.amazon.awscdk.services.cloudwatch.CfnAlarm.MetricProperty;
import software.amazon.awscdk.services.cloudwatch.CfnAlarm.MetricStatProperty;
import software.amazon.awscdk.services.cloudwatch.ComparisonOperator;
import software.amazon.awscdk.services.cloudwatch.CreateAlarmOptions;
import software.amazon.awscdk.services.cloudwatch.Dashboard;
import software.amazon.awscdk.services.cloudwatch.GraphWidget;
import software.amazon.awscdk.services.cloudwatch.GraphWidgetView;
import software.amazon.awscdk.services.cloudwatch.IAlarm;
import software.amazon.awscdk.services.cloudwatch.IWidget;
import software.amazon.awscdk.services.cloudwatch.Metric;
import software.amazon.awscdk.services.ec2.CfnEIP;
import software.amazon.awscdk.services.ec2.CfnEIPAssociation;
import software.amazon.awscdk.services.ec2.CfnInstance;
import software.amazon.awscdk.services.ec2.CfnInstance.BlockDeviceMappingProperty;
import software.amazon.awscdk.services.ec2.CfnInstance.EbsProperty;
import software.amazon.awscdk.services.ec2.CfnInstance.LaunchTemplateSpecificationProperty;
import software.amazon.awscdk.services.ec2.CfnInternetGateway;
import software.amazon.awscdk.services.ec2.CfnInternetGatewayProps;
import software.amazon.awscdk.services.ec2.CfnLaunchTemplate;
import software.amazon.awscdk.services.ec2.CfnLaunchTemplate.LaunchTemplateDataProperty;
import software.amazon.awscdk.services.ec2.CfnLaunchTemplate.LaunchTemplateTagSpecificationProperty;
import software.amazon.awscdk.services.ec2.CfnNatGateway;
import software.amazon.awscdk.services.ec2.CfnNetworkAcl;
import software.amazon.awscdk.services.ec2.CfnNetworkAclEntry;
import software.amazon.awscdk.services.ec2.CfnRoute;
import software.amazon.awscdk.services.ec2.CfnRouteTable;
import software.amazon.awscdk.services.ec2.CfnSecurityGroup;
import software.amazon.awscdk.services.ec2.CfnSecurityGroup.IngressProperty;
import software.amazon.awscdk.services.ec2.CfnSubnet;
import software.amazon.awscdk.services.ec2.CfnSubnetNetworkAclAssociation;
import software.amazon.awscdk.services.ec2.CfnSubnetRouteTableAssociation;
import software.amazon.awscdk.services.ec2.CfnVPC;
import software.amazon.awscdk.services.ec2.CfnVPCGatewayAttachment;
import software.amazon.awscdk.services.ec2.CfnVPCGatewayAttachmentProps;
import software.amazon.awscdk.services.efs.CfnFileSystem;
import software.amazon.awscdk.services.efs.CfnFileSystem.BackupPolicyProperty;
import software.amazon.awscdk.services.efs.CfnFileSystem.ElasticFileSystemTagProperty;
import software.amazon.awscdk.services.efs.CfnMountTarget;
import software.amazon.awscdk.services.elasticloadbalancingv2.CfnListener;
import software.amazon.awscdk.services.elasticloadbalancingv2.CfnListener.ActionProperty;
import software.amazon.awscdk.services.elasticloadbalancingv2.CfnLoadBalancer;
import software.amazon.awscdk.services.elasticloadbalancingv2.CfnLoadBalancer.SubnetMappingProperty;
import software.amazon.awscdk.services.elasticloadbalancingv2.CfnTargetGroup;
import software.amazon.awscdk.services.elasticloadbalancingv2.CfnTargetGroup.MatcherProperty;
import software.amazon.awscdk.services.elasticloadbalancingv2.CfnTargetGroup.TargetGroupAttributeProperty;
import software.amazon.awscdk.services.globalaccelerator.CfnAccelerator;
import software.amazon.awscdk.services.iam.AnyPrincipal;
import software.amazon.awscdk.services.iam.CfnAccessKey;
import software.amazon.awscdk.services.iam.CfnGroup;
import software.amazon.awscdk.services.iam.CfnPolicy;
import software.amazon.awscdk.services.iam.CfnRole;
import software.amazon.awscdk.services.iam.CfnUser;
import software.amazon.awscdk.services.iam.CfnUser.LoginProfileProperty;
import software.amazon.awscdk.services.iam.CfnUser.PolicyProperty;
import software.amazon.awscdk.services.iam.Effect;
import software.amazon.awscdk.services.iam.Group;
import software.amazon.awscdk.services.iam.ManagedPolicy;
import software.amazon.awscdk.services.iam.PolicyDocument;
import software.amazon.awscdk.services.iam.PolicyStatement;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.amazon.awscdk.services.iam.StarPrincipal;
import software.amazon.awscdk.services.iam.User;
import software.amazon.awscdk.services.lambda.AssetCode;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.route53.CfnHostedZone;
import software.amazon.awscdk.services.route53.CfnRecordSet;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.CfnBucket;
import software.amazon.awscdk.services.s3.CfnBucket.CorsConfigurationProperty;
import software.amazon.awscdk.services.s3.CfnBucket.CorsRuleProperty;
import software.amazon.awscdk.services.s3.CfnBucket.WebsiteConfigurationProperty;
import software.amazon.awscdk.services.s3.CfnBucketPolicy;
import software.amazon.awscdk.services.s3.IBucket;
import software.amazon.awscdk.services.s3.deployment.BucketDeployment;
import software.amazon.awscdk.services.s3.deployment.Source;
import software.amazon.awscdk.services.secretsmanager.Secret;
import software.amazon.awscdk.services.secretsmanager.SecretStringGenerator;
import software.amazon.awscdk.services.sns.CfnTopic;
import software.amazon.awscdk.services.sns.ITopic;
import software.amazon.awscdk.services.sns.Topic;
import software.amazon.awscdk.services.sns.subscriptions.LambdaSubscription;
import software.amazon.jsii.JsiiObject;
//import software.amazon.awscdk.services.stepfunctions.Map;
import software.constructs.Construct;

//cdk deploy --profile test2 --require-approval never
//TODO:先用cli建key pair，時間上是允許的
//IAM考慮另外建，安全性比較高，權限設定也會比較簡單
//aws ec2 create-key-pair --profile test2 --key-name awc-gci-aws07-ca --key-type rsa --query "KeyMaterial" --output text > awc-gci-aws07-ca.pem
public class HelloCdkStack extends Stack {

	public HelloCdkStack(final Construct scope, final String id) {
		this(scope, id, null);
	}

	//如172.18.1.0/24
	//172.18叫做網路編號
	//1叫做子網路
	//0/24叫做主機編號
	
	static String networkIds = "172.18";
	static String classB = networkIds + ".0.0/16";
	static String publicANetworkId = ".1";
	static String privateANetworkId = ".2";
	static String publicBNetworkId = ".3";
	static String privateBNetworkId = ".4";
	static String testANetworkId = ".100";
	static String testBNetworkId = ".101";
	static String test01NetworkId = ".110";
	static String test02NetworkId = ".111";
	static String hostIds = ".0/24";
	
//	既有ami使用的關鍵字整理如下
//	amzn2-ami-hvm-2.0開頭，x86_64-gp2結尾
//	b7ee8a69-ee97-4a49-9e68-afaee216db2e-ami-0042af67f8e4dcc20.4
//	RHEL-7.7_HVM-20190923-x86_64-0-Hourly2-GP2
//	在這裡找 EC2/ AMI Catalog/ Community AMIs
	static String ami01 = "ami-0729cd65c1a99b0c9";//跳版機ca
	static String ami02 = "ami-098f55b4287a885ba";//ap ca
	static String ami03 = "ami-07d8d14365439bc6e";//db ca
	
	static String keyPairs = "awc-gci-aws07-ca";//ca
	static String s3Dir = "./asset/s3";
	static String remoteBackup = "arn:aws:backup:us-west-2:481311441598:backup-vault:sam-manual-bak-vault";
	//注意myAuthorizerFunction01的準備
	
	//AWS Constants
//	https://us-east-1.console.aws.amazon.com/cloudfront/v3/home?region=us-west-1#/policies/cache
	static String MANAGED_CACHINGOPTIMIZED = "658327ea-f89d-4fab-a63d-7e88639e58f6";//Managed-CachingOptimized
//	https://us-east-1.console.aws.amazon.com/cloudfront/v3/home?region=us-west-1#/policies/origin
	static String MANAGED_CACHINGDISABLED = "4135ea2d-6df8-44a3-9df3-4b5a84be39ad";//Managed-CachingDisabled
	
	static String MANAGED_ALLVIEWER = "216adef6-5c7f-47e4-b989-5492eafa07d3";//Managed-AllViewer
	static String MANAGED_ELEMENTAL_MEDIATAILOR_PERSONALIZEDMANIFESTS = "775133bc-15f2-49f9-abea-afb2e0bf67d2";//Managed-Elemental-MediaTailor-PersonalizedManifests
	
	static String GA_ZONE_ID = "Z2BJ6XQ5FK7U4H";
	static String CF_ZONE_ID = "Z2FDTNDATAQYW2";
	
	//cdk destroy
	static boolean destroy = true;//雖然刪除bucket與vault失敗，但好像其它的都刪掉了
	public HelloCdkStack(final Construct scope, final String id, final StackProps props) {
		super(scope, id, props);
		
		String account = props.getEnv().getAccount();
		String region = props.getEnv().getRegion();
				
		java.util.List<String> availabilityZones = this.getAvailabilityZones();
		
		String az1 = availabilityZones.get(1 % availabilityZones.size());
		String az2 = availabilityZones.get(2 % availabilityZones.size());
		
		if (destroy) {
			return;
		}
		//L2太高級了，建了一大堆東西
		//如果我請它建了一大堆東西，然後再把東西改掉
		//不如就單純建我要的東西就好
//		https://docs.aws.amazon.com/zh_tw/vpc/latest/userguide/working-with-vpcs.html
		CfnVPC vpc = CfnVPC.Builder.create(this, "myVpc")
			.cidrBlock(classB)
			.tags(Name.of("GCI-AWS07-ca VPC"))
			.enableDnsHostnames(true)
			.build();
		//=================================================================
		CfnSubnet publicA = null;
		CfnSubnet privateA = null;
		CfnSubnet publicB = null;
		CfnSubnet privateB = null;
		CfnSubnet testA = null;
		CfnSubnet testB = null;
		CfnSubnet test01 = null;
		CfnSubnet test02 = null;
		{
			CfnSubnet myPublicA = CfnSubnet.Builder.create(this, "myPublicA")
				.cidrBlock(networkIds + publicANetworkId + hostIds)
				.tags(Name.of("publicA"))
				.availabilityZone(az1)
				.vpcId(vpc.getRef()).build();
			publicA = myPublicA;
			
			CfnSubnet myPrivateA = CfnSubnet.Builder.create(this, "myPrivateA")
				.cidrBlock(networkIds + privateANetworkId + hostIds)
				.tags(Name.of("privateA"))
				.availabilityZone(az1)
				.vpcId(vpc.getRef()).build();
			privateA = myPrivateA;
			
			CfnSubnet myPublicB = CfnSubnet.Builder.create(this, "myPublicB")
				.cidrBlock(networkIds + publicBNetworkId + hostIds)
				.tags(Name.of("publicB"))
				.availabilityZone(az2)
				.vpcId(vpc.getRef()).build();
			publicB = myPublicB;
			
			CfnSubnet myPrivateB = CfnSubnet.Builder.create(this, "myPrivateB")
				.cidrBlock(networkIds + privateBNetworkId + hostIds)
				.tags(Name.of("privateB"))
				.availabilityZone(az2)
				.vpcId(vpc.getRef()).build();
			privateB = myPrivateB;
			
			CfnSubnet myTestA = CfnSubnet.Builder.create(this, "myTestA")
				.cidrBlock(networkIds + testANetworkId + hostIds)
				.tags(Name.of("GCI-AWS07-TESTA"))
				.availabilityZone(az1)
				.vpcId(vpc.getRef()).build();
			testA = myTestA;
			
			CfnSubnet myTestB = CfnSubnet.Builder.create(this, "myTestB")
				.cidrBlock(networkIds + testBNetworkId + hostIds)
				.tags(Name.of("GCI-AWS07-TESTB"))
				.availabilityZone(az2)
				.vpcId(vpc.getRef()).build();
			testB = myTestB;

			CfnSubnet myTest01 = CfnSubnet.Builder.create(this, "myTest01")
				.cidrBlock(networkIds + test01NetworkId + hostIds)
				.tags(Name.of("GCI-AWS07-TEST01"))
				.availabilityZone(az1)
				.vpcId(vpc.getRef()).build();
			test01 = myTest01;
		
			CfnSubnet myTest02 = CfnSubnet.Builder.create(this, "myTest02")
				.cidrBlock(networkIds + test02NetworkId + hostIds)
				.tags(Name.of("GCI-AWS07-TEST02"))
				.availabilityZone(az2)
				.vpcId(vpc.getRef()).build();
			test02 = myTest02;
		}
		//=================================================================
//		https://docs.aws.amazon.com/zh_tw/vpc/latest/userguide/VPC_Internet_Gateway.html
		CfnInternetGateway igw = null;
		{
			// 建立igw
			CfnInternetGateway myigw = new CfnInternetGateway(this, "myigw",
				CfnInternetGatewayProps.builder()
				.tags(Name.of("GCI-AWS07-igw"))//從gw改成igw
				.build());
			igw = myigw;
			
			// igw綁定vpc
			new CfnVPCGatewayAttachment(this, "myigwa", CfnVPCGatewayAttachmentProps.builder()
				.internetGatewayId(igw.getRef()).vpcId(vpc.getRef()).build());
		}
		
//		https://docs.aws.amazon.com/zh_tw/vpc/latest/userguide/vpc-nat-gateway.html
		CfnNatGateway nat1 = null;
		{
			CfnNatGateway myNat1 = CfnNatGateway.Builder.create(this, "myNat1")
				.tags(List.of(CfnTag.builder().key("Name").value("GCI-AWS07-nat-1").build()))
				.connectivityType("public")
				.allocationId(CfnEIP.Builder.create(this, "myIp1")
					.tags(Name.of("GCI-AWS07-natip-1"))
					.build().getAttrAllocationId())
				.subnetId(publicA.getRef())
				.build();
			nat1 = myNat1;
		}
		CfnNatGateway nat2 = null;
		{
			CfnNatGateway myNat2 = CfnNatGateway.Builder.create(this, "myNat2")
				.tags(List.of(CfnTag.builder().key("Name").value("GCI-AWS07-nat-2").build()))
				.connectivityType("public")
				.allocationId(CfnEIP.Builder.create(this, "myIp2")
					.tags(Name.of("GCI-AWS07-natip-2"))
					.build().getAttrAllocationId())
				.subnetId(publicB.getRef())
				.build();
			nat2 = myNat2;
		}
		//=================================================================
//		https://docs.aws.amazon.com/zh_tw/vpc/latest/userguide/VPC_Route_Tables.html
		CfnRouteTable Public_subnet = null;
		{
			CfnRouteTable myRouteTable1 = CfnRouteTable.Builder.create(this, "myRouteTable1")
				.tags(Name.of("Public_subnet"))
				.vpcId(vpc.getRef())
				.build();
			Public_subnet = myRouteTable1;

			//建立igw連結，應該有別的做法
			CfnRoute.Builder.create(this, "myRoute1")
				.destinationCidrBlock("0.0.0.0/0")
				.routeTableId(myRouteTable1.getRef())
				.gatewayId(igw.getRef()).build();
			
			CfnSubnetRouteTableAssociation.Builder.create(this, "myRouteTable1_subnet1")
				.routeTableId(myRouteTable1.getRef())
				.subnetId(publicA.getRef()).build();
			
			CfnSubnetRouteTableAssociation.Builder.create(this, "myRouteTable1_subnet2")
				.routeTableId(myRouteTable1.getRef())
				.subnetId(publicB.getRef()).build();

			CfnSubnetRouteTableAssociation.Builder.create(this, "myRouteTable1_subnet3")
				.routeTableId(myRouteTable1.getRef())
				.subnetId(testA.getRef()).build();
			CfnSubnetRouteTableAssociation.Builder.create(this, "myRouteTable1_subnet4")
				.routeTableId(myRouteTable1.getRef())
				.subnetId(testB.getRef()).build();
		}
		CfnRouteTable PrivateA_subnet = null;
		{
			CfnRouteTable myRouteTable2 = CfnRouteTable.Builder.create(this, "myRouteTable2")
				.tags(Name.of("PrivateA_subnet"))
				.vpcId(vpc.getRef())
				.build();
			PrivateA_subnet = myRouteTable2;
			
			//建立nat1連結
			CfnRoute.Builder.create(this, "myRoute2")
				.destinationCidrBlock("0.0.0.0/0")
				.routeTableId(myRouteTable2.getRef())
				.natGatewayId(nat1.getRef()).build();
			
			CfnSubnetRouteTableAssociation.Builder.create(this, "myRouteTable2_subnet1")
				.routeTableId(myRouteTable2.getRef())
				.subnetId(privateA.getRef()).build();
			
			CfnSubnetRouteTableAssociation.Builder.create(this, "myRouteTable2_subnet2")
				.routeTableId(myRouteTable2.getRef())
				.subnetId(test01.getRef()).build();
		}
		CfnRouteTable PrivateB_subnet = null;
		{
			CfnRouteTable myRouteTable3 = CfnRouteTable.Builder.create(this, "myRouteTable3")
				.tags(Name.of("PrivateB_subnet"))
				.vpcId(vpc.getRef())
				.build();
			PrivateB_subnet = myRouteTable3;
			
			//建立nat2連結
			CfnRoute.Builder.create(this, "myRoute3")
				.destinationCidrBlock("0.0.0.0/0")
				.routeTableId(myRouteTable3.getRef())
				.natGatewayId(nat2.getRef()).build();
			
			CfnSubnetRouteTableAssociation.Builder.create(this, "myRouteTable3_subnet1")
				.routeTableId(myRouteTable3.getRef())
				.subnetId(privateB.getRef()).build();
			
			CfnSubnetRouteTableAssociation.Builder.create(this, "myRouteTable3_subnet2")
				.routeTableId(myRouteTable3.getRef())
				.subnetId(test02.getRef()).build();
		}
		//=================================================================
//		https://docs.aws.amazon.com/zh_tw/vpc/latest/userguide/VPC_Security.html#VPC_Security_Comparison
//		比較安全群組和網路 ACL
			
		//存取控制列表
		//https://docs.aws.amazon.com/zh_tw/vpc/latest/userguide/vpc-network-acls.html
		{
			CfnNetworkAcl subnetAllAcl = CfnNetworkAcl.Builder.create(this, "myNetworkAcl1")
				.tags(Name.of("subnet-all"))
				.vpcId(vpc.getRef())
				.build();

			{
				CfnSubnetNetworkAclAssociation.Builder.create(this, "myNetworkAcl1_subnet1")
					.networkAclId(subnetAllAcl.getRef())
					.subnetId(publicA.getRef())
					.build();
				
				CfnSubnetNetworkAclAssociation.Builder.create(this, "myNetworkAcl1_subnet2")
					.networkAclId(subnetAllAcl.getRef())
					.subnetId(privateA.getRef())
					.build();
				
				CfnSubnetNetworkAclAssociation.Builder.create(this, "myNetworkAcl1_subnet3")
					.networkAclId(subnetAllAcl.getRef())
					.subnetId(publicB.getRef())
					.build();
				
				CfnSubnetNetworkAclAssociation.Builder.create(this, "myNetworkAcl1_subnet4")
					.networkAclId(subnetAllAcl.getRef())
					.subnetId(privateB.getRef())
					.build();
				
				CfnSubnetNetworkAclAssociation.Builder.create(this, "myNetworkAcl1_subnet5")
					.networkAclId(subnetAllAcl.getRef())
					.subnetId(test01.getRef())
					.build();
				
				CfnSubnetNetworkAclAssociation.Builder.create(this, "myNetworkAcl1_subnet6")
					.networkAclId(subnetAllAcl.getRef())
					.subnetId(test02.getRef())
					.build();
			}
			{
				CfnNetworkAclEntry.Builder.create(this, "myNetworkAcl1_Inbound1")
					.cidrBlock("0.0.0.0/0")
					.ruleNumber(100)
					.protocol(-1)
					.ruleAction("allow")
					.networkAclId(subnetAllAcl.getRef())
					.build();
				CfnNetworkAclEntry.Builder.create(this, "myNetworkAcl1_Outbound1")
					.cidrBlock("0.0.0.0/0")
					.ruleNumber(100)
					.protocol(-1)
					.ruleAction("allow")
					.networkAclId(subnetAllAcl.getRef())
					.egress(true)
					.build();
			}
		}
		{
			CfnNetworkAcl testABAcl = CfnNetworkAcl.Builder.create(this, "myNetworkAcl2")
				.tags(Name.of("TestAB"))
				.vpcId(vpc.getRef())
				.build();
			
			{
				CfnSubnetNetworkAclAssociation.Builder.create(this, "myNetworkAcl2_subnet1")
					.networkAclId(testABAcl.getRef())
					.subnetId(testA.getRef())
					.build();
			
				CfnSubnetNetworkAclAssociation.Builder.create(this, "myNetworkAcl2_subnet2")
					.networkAclId(testABAcl.getRef())
					.subnetId(testB.getRef())
					.build();
			}
			{
				CfnNetworkAclEntry.Builder.create(this, "myNetworkAcl2_Inbound1")
					.cidrBlock("0.0.0.0/0")
					.ruleNumber(100)
					.protocol(-1)
					.ruleAction("allow")
					.networkAclId(testABAcl.getRef())
					.build();
			
				CfnNetworkAclEntry.Builder.create(this, "myNetworkAcl2_Inbound2")
					.cidrBlock(networkIds + publicANetworkId + hostIds)
	//				.cidrBlock("172.18.1.0/24")
					.ruleNumber(99)
					.protocol(-1)
					.ruleAction("deny")
					.networkAclId(testABAcl.getRef())
					.build();
				
				CfnNetworkAclEntry.Builder.create(this, "myNetworkAcl2_Inbound3")
					.cidrBlock(networkIds + privateANetworkId + hostIds)//改了一下順序
	//				.cidrBlock("172.18.3.0/24")
					.ruleNumber(98)
					.protocol(-1)
					.ruleAction("deny")
					.networkAclId(testABAcl.getRef())
					.build();
				
				CfnNetworkAclEntry.Builder.create(this, "myNetworkAcl2_Inbound4")
					.cidrBlock(networkIds + publicBNetworkId + hostIds)//改了一下順序
	//				.cidrBlock("172.18.2.0/24")
					.ruleNumber(97)
					.protocol(-1)
					.ruleAction("deny")
					.networkAclId(testABAcl.getRef())
					.build();
				
				CfnNetworkAclEntry.Builder.create(this, "myNetworkAcl2_Inbound5")
					.cidrBlock(networkIds + privateBNetworkId + hostIds)
	//				.cidrBlock("172.18.4.0/24")
					.ruleNumber(96)
					.protocol(-1)
					.ruleAction("deny")
					.networkAclId(testABAcl.getRef())
					.build();
				
				CfnNetworkAclEntry.Builder.create(this, "myNetworkAcl2_Outbound1")
					.cidrBlock("0.0.0.0/0")
					.ruleNumber(100)
					.protocol(-1)
					.ruleAction("allow")
					.networkAclId(testABAcl.getRef())
					.egress(true)
					.build();
			}
		}
		//=================================================================
//		https://docs.aws.amazon.com/zh_tw/vpc/latest/userguide/VPC_SecurityGroups.html
		//註:下面開始，如果不想要提示，可以加--require-approval never
		CfnSecurityGroup securityGroupTestAB = null;
		CfnSecurityGroup securityGroupTestGroup = null;
		CfnSecurityGroup securityGroupWebGroup = null;
		CfnSecurityGroup securityGroupApiGroup = null;
		CfnSecurityGroup securityGroupWebElb = null;//特別
		{
			//改了一下順序
			CfnSecurityGroup mySecurityGroup1 = CfnSecurityGroup.Builder.create(this, "mySecurityGroup1")
				.groupName("gci-aws07-TestAB")//不設會被蓋掉為HelloCdkStack-mySecurityGroup1-Z6WUCAP7T2EA
				.groupDescription("gci-aws07-TestAB")//應該是因為這個原因，所以Edward說要重建
				.tags(Name.of("gci-aws07-TestAB"))//TAG是console用的，上面是API會用的吧
				.vpcId(vpc.getRef())
				.securityGroupIngress(List.of(
					CfnSecurityGroupIngressBug.of("tcp", 32321, "61.66.217.64/26", "Neurv-oa")
					,CfnSecurityGroupIngressBug.of("tcp", 32321, "207.226.152.106/32", "Neutec-oa")
					,CfnSecurityGroupIngressBug.of("tcp", 32321, "59.120.38.160/27", "Neurv-oa")
					,CfnSecurityGroupIngressBug.of("tcp", 32321, "211.20.2.194/32", "Neutec-oa")
					,CfnSecurityGroupIngressBug.of("tcp", 32321, "61.200.83.82/32", "Neutec-oa")
					,CfnSecurityGroupIngressBug.of("tcp", 32321, "14.136.94.96/27", "Neurv-oa")
					,CfnSecurityGroupIngressBug.of("tcp", 32321, "218.32.64.162/32", "Neutec-oa")
					,CfnSecurityGroupIngressBug.of("tcp", 32321, "218.32.65.98/32", "Neutec-oa")
					))
				.build();
			securityGroupTestAB = mySecurityGroup1;
			
			CfnSecurityGroup mySecurityGroup2 = CfnSecurityGroup.Builder.create(this, "mySecurityGroup2")
				.groupName("gci-aws07-test-group")
				.groupDescription("gci-aws07-test-group")
				.tags(Name.of("gci-aws07-test-group"))
				.vpcId(vpc.getRef())
				.securityGroupIngress(List.of(
					CfnSecurityGroupIngressBug.of("tcp", 22, 32321, classB, "classB")))
				.build();
			securityGroupTestGroup = mySecurityGroup2;
			
			CfnSecurityGroup.Builder.create(this, "mySecurityGroup3")
				.groupName("gci-aws07-redis-group")
				.groupDescription("gci-aws07-redis-group")
				.tags(Name.of("gci-aws07-redis-group"))
				.vpcId(vpc.getRef())
				.securityGroupIngress(List.of(
					CfnSecurityGroupIngressBug.of("tcp", 56123, classB, "classB")
					,CfnSecurityGroupIngressBug.of("tcp", 7000, 7003, classB, "classB")
					,CfnSecurityGroupIngressBug.of("tcp", 17000, 17003, classB, "classB")))
				.build();
			
			CfnSecurityGroup.Builder.create(this, "mySecurityGroup4")
				.groupName("gci-aws07-db-group")
				.groupDescription("gci-aws07-db-group")
				.tags(Name.of("gci-aws07-db-group"))
				.vpcId(vpc.getRef())
				.securityGroupIngress(List.of(
					CfnSecurityGroupIngressBug.of(classB, "classB")))
				.build();
			
			//特別，給LB用的
			CfnSecurityGroup mySecurityGroup5 = CfnSecurityGroup.Builder.create(this, "mySecurityGroup5")
				.groupName("gci-aws07-web-elb")
				.groupDescription("gci-aws07-web-elb")
				.tags(Name.of("gci-aws07-web-elb"))
				.vpcId(vpc.getRef())
				.securityGroupIngress(List.of(
					CfnSecurityGroupIngressBug.of("tcp", 443, classB, "classB")
					,CfnSecurityGroupIngressBug.of("tcp", 80, classB, "classB")))
				.build();
			securityGroupWebElb = mySecurityGroup5;
			
			CfnSecurityGroup.Builder.create(this, "mySecurityGroup6")
				.groupName("gci-aws07-fetch-group")
				.groupDescription("gci-aws07-fetch-group")
				.tags(Name.of("gci-aws07-fetch-group"))
				.vpcId(vpc.getRef())
				.securityGroupIngress(List.of(
					CfnSecurityGroupIngressBug.of(classB, "classB")))
				.build();
			
			CfnSecurityGroup mySecurityGroup7 = CfnSecurityGroup.Builder.create(this, "mySecurityGroup7")
				.groupName("gci-aws07-api-group")
				.groupDescription("gci-aws07-api-group")
				.tags(Name.of("gci-aws07-api-group"))
				.vpcId(vpc.getRef())
				.securityGroupIngress(List.of(
					CfnSecurityGroupIngressBug.of(classB, "classB")))
				.build();
			securityGroupApiGroup = mySecurityGroup7;
			
			CfnSecurityGroup mySecurityGroup8 = CfnSecurityGroup.Builder.create(this, "mySecurityGroup8")
				.groupName("gci-aws07-web-group")
				.groupDescription("gci-aws07-web-group")
				.tags(Name.of("gci-aws07-web-group"))
				.vpcId(vpc.getRef())
				.securityGroupIngress(List.of(
					CfnSecurityGroupIngressBug.of("tcp", 22, 56123, classB, "classB")))
				.build();
			securityGroupWebGroup = mySecurityGroup8;
		}
		
		//監控的內容，和建立的EC2會有關連
		GraphWidget apCpu = null;
		GraphWidget apMemory = null;
		GraphWidget apiNetwork = null;
		GraphWidget webNetwork = null;
		GraphWidget dbNetwork = null;
		GraphWidget dbCpu = null;
		GraphWidget redisCpu = null;

		GraphWidget mdbCpu = null;
		GraphWidget mdbNetwork = null;
		GraphWidget mdbNetworkPackets = null;
		
//		GraphWidget mdbVolumeReadOps = null;
//		GraphWidget mdbVolumeWriteOps = null;
		
		//先放這，晚點重建再改ID
		CfnInstance gciAws07web01;
		{
			//ec2不要掛targetgroup，獨外做就好了
			String testAName = "gci-aws07-TestA";
			CfnInstance gciAws07TestA = CfnInstance.Builder.create(this, "gci-aws07-TestA")
				.tags(Name.of(testAName))
				.subnetId(testA.getRef())
				.availabilityZone(az1)
				.securityGroupIds(List.of(securityGroupTestAB.getRef()))
				.instanceType("t3.micro")
				.monitoring(false)
				.privateIpAddress("172.18.100.31")
				.imageId(ami01)//ca
				.keyName(keyPairs)
				.userData(Fn.base64(Fn.join("\n", List.of(
					"#!/bin/bash",
					"yum install -y https://dl.fedoraproject.org/pub/epel/epel-release-latest-7.noarch.rpm",
					"yum install google-authenticator.x86_64 -y",
					"sed -i 's/\\<PasswordAuthentication no\\>/PasswordAuthentication yes/g' /etc/ssh/sshd_config",
					"sed -i 's/\\<ChallengeResponseAuthentication no\\>/ChallengeResponseAuthentication yes/g' /etc/ssh/sshd_config",
					"sed -i '$aPort 32321' /etc/ssh/sshd_config",
	//				"sed -i '$aPort 22' /etc/ssh/sshd_config",
					"service sshd restart",
					"echo 'auth required pam_google_authenticator.so nullok' >> /etc/pam.d/sshd",
					"sudo hostnamectl set-hostname --static "+testAName//有很多方法，這個方法比較簡單
				))))
				.build();
			
			CfnEIPAssociation.Builder.create(this, "myIp3Association")
				.eip(CfnEIP.Builder.create(this, "myIp3")
					.tags(Name.of("GCI-AWS07-TestA-ip-1")).build().getRef())
				.instanceId(gciAws07TestA.getRef())
				.build();
			
			String test01Name = "gci-aws07-Test01";
			CfnInstance gciAws07Test01 = CfnInstance.Builder.create(this, "gci-aws07-Test01")
				.tags(Name.of(test01Name))
				.subnetId(test01.getRef())
				.availabilityZone(az1)
				.securityGroupIds(List.of(securityGroupTestGroup.getRef()))
				.instanceType("t3.micro")
				.monitoring(false)
				.privateIpAddress("172.18.110.41")
				.imageId(ami01)//ca
				.keyName(keyPairs)
				.blockDeviceMappings(List.of(
					BlockDeviceMappingProperty.builder()
						.ebs(EbsProperty.builder()
							.volumeSize(50)
							.deleteOnTermination(true)
							.build())
						.deviceName("/dev/xvda")
						.build()
				))
				.userData(Fn.base64(Fn.join("\n", List.of(
					"#!/bin/bash",
					"sed -i 's/\\<PasswordAuthentication no\\>/PasswordAuthentication yes/g' /etc/ssh/sshd_config",
					"sed -i '$aPort 32321' /etc/ssh/sshd_config",
//					"sed -i '$aPort 22' /etc/ssh/sshd_config",
					"service sshd restart",
					"sudo hostnamectl set-hostname --static "+test01Name//有很多方法，這個方法比較簡單
				))))
				.build();
			
			String api01Name = "gci-aws07-api01";
			CfnInstance gciAws07api01 = CfnInstance.Builder.create(this, "gciAws07api01")
				.tags(Name.of(api01Name))
				.subnetId(privateA.getRef())
				.availabilityZone(az1)
				.securityGroupIds(List.of(securityGroupWebGroup.getRef()))
//				.instanceType("m5.2xlarge")
//				.instanceType("m5.large")//TODO:測試
				.instanceType("t3.micro")//TODO:測試
				.monitoring(false)
				.privateIpAddress("172.18.2.61")
				.imageId(ami02)//ca
				.keyName(keyPairs)
				.blockDeviceMappings(List.of(
					BlockDeviceMappingProperty.builder()
						.ebs(EbsProperty.builder()
							.volumeSize(8)
//							.deleteOnTermination(false)//因為防呆?
							.deleteOnTermination(true)//TODO:測試時不要防呆
							.build())
						.deviceName("/dev/sda1")
						.build()
					, BlockDeviceMappingProperty.builder()
						.ebs(EbsProperty.builder()
//							.volumeSize(200)
//							.iops(3000)
//							.volumeType("gp3")
							.volumeSize(8)//TODO:測試
//							.iops(100)//TODO:測試
//							.volumeType("gp3")//TODO:測試
							.volumeType("gp2")
//							.deleteOnTermination(false)//因為防呆?
							.deleteOnTermination(true)//TODO:測試時不要防呆
							.build())
						.deviceName("/dev/sdb")
						.build()
					))
					.userData(Fn.base64(Fn.join("\n", List.of(
						"#!/bin/bash",
						"sed -i 's/\\<PasswordAuthentication no\\>/PasswordAuthentication yes/g' /etc/ssh/sshd_config", 
						"sed -i '$aPermitRootLogin yes' /etc/ssh/sshd_config",
						"sed -i '$aPort 56123' /etc/ssh/sshd_config",
						"service sshd restart",
						"mkdir /usr/local/service",
						"mkfs -t xfs /dev/nvme1n1",
						"mount /dev/nvme1n1 /usr/local/service",
						"sudo hostnamectl set-hostname --static "+api01Name//有很多方法，這個方法比較簡單
					))))
				.build();
			
			String web01Name = "gci-aws07-web01";
//			CfnInstance 
			gciAws07web01 = CfnInstance.Builder.create(this, "gciAws07web01")
//				.tags(Name.of(web01Name))
				.tags(List.of(CfnTag.builder().key("Name").value(web01Name).build(),
					CfnTag.builder().key("backup").value("true").build()
				))
				.subnetId(privateA.getRef())
				.availabilityZone(az1)
				.securityGroupIds(List.of(securityGroupWebGroup.getRef()))
//				.instanceType("m5.2xlarge")
//				.instanceType("m5.large")//TODO:測試
				.instanceType("t3.micro")//TODO:測試
				.monitoring(false)
				.privateIpAddress("172.18.2.11")
				.imageId(ami02)//ca
				.keyName(keyPairs)
				.blockDeviceMappings(List.of(
					BlockDeviceMappingProperty.builder()
						.ebs(EbsProperty.builder()
							.volumeSize(8)
//							.deleteOnTermination(false)//因為防呆?
							.deleteOnTermination(true)//TODO:測試時不要防呆
							.build())
						.deviceName("/dev/sda1")
						.build()
					, BlockDeviceMappingProperty.builder()
						.ebs(EbsProperty.builder()
//							.volumeSize(200)
//							.iops(3000)
							.volumeSize(8)//TODO:測試
//							.iops(100)//TODO:測試
//							.volumeType("gp3")//TODO:測試
							.volumeType("gp2")
//							.deleteOnTermination(false)//因為防呆?
							.deleteOnTermination(true)//TODO:測試時不要防呆
							.build())
						.deviceName("/dev/sdb")
						.build()
					))
					.userData(Fn.base64(Fn.join("\n", List.of(
						"#!/bin/bash",
						"sed -i 's/\\<PasswordAuthentication no\\>/PasswordAuthentication yes/g' /etc/ssh/sshd_config", 
						"sed -i '$aPermitRootLogin yes' /etc/ssh/sshd_config",
						"sed -i '$aPort 56123' /etc/ssh/sshd_config",
//						"sed -i '$aPort 22' /etc/ssh/sshd_config",
						"service sshd restart",
						"mkdir /usr/local/service",
						"mkfs -t xfs /dev/nvme1n1",
						"mount /dev/nvme1n1 /usr/local/service",
						"sudo hostnamectl set-hostname --static "+web01Name//有很多方法，這個方法比較簡單
					))))
				.build();
			CfnInstance gciAws07Redis01 = null;
			{
				String redis01Name = "gci-aws07-redis01";
				CfnInstance myGciAws07Redis01 = CfnInstance.Builder.create(this, "myGciAws07Redis01")
					.tags(Name.of(redis01Name))
					.subnetId(privateA.getRef())
					.availabilityZone(az1)
					.securityGroupIds(List.of(securityGroupWebGroup.getRef()))
//					.instanceType("m5.2xlarge")
//					.instanceType("m5.large")//TODO:測試
					.instanceType("t3.micro")//TODO:測試
					.monitoring(false)
					.privateIpAddress("172.18.2.151")
					.imageId(ami02)//ca
					.keyName(keyPairs)
					.blockDeviceMappings(List.of(
						BlockDeviceMappingProperty.builder()
							.ebs(EbsProperty.builder()
								.volumeSize(8)
//								.deleteOnTermination(false)//因為防呆?
								.deleteOnTermination(true)//TODO:測試時不要防呆
								.build())
							.deviceName("/dev/sda1")
							.build()
						, BlockDeviceMappingProperty.builder()
							.ebs(EbsProperty.builder()
//								.volumeSize(50)
//								.iops(3000)
								.volumeSize(8)//TODO:測試
//								.iops(100)//TODO:測試
//								.volumeType("gp3")//TODO:測試
								.volumeType("gp2")
//								.deleteOnTermination(false)//因為防呆?
								.deleteOnTermination(true)//TODO:測試時不要防呆
								.build())
							.deviceName("/dev/sdb")
							.build()
						))
						.userData(Fn.base64(Fn.join("\n", List.of(
							"#!/bin/bash",
							"sed -i 's/\\<PasswordAuthentication no\\>/PasswordAuthentication yes/g' /etc/ssh/sshd_config", 
							"sed -i '$aPermitRootLogin yes' /etc/ssh/sshd_config",
							"sed -i '$aPort 56123' /etc/ssh/sshd_config",
//							"sed -i '$aPort 22' /etc/ssh/sshd_config",
							"service sshd restart",
							"mkdir /usr/local/service",
							"mkfs -t xfs /dev/nvme1n1",
							"mount /dev/nvme1n1 /usr/local/service",
							"sudo hostnamectl set-hostname --static "+redis01Name//有很多方法，這個方法比較簡單
						))))
					.build();
				gciAws07Redis01 = myGciAws07Redis01;
			}
			
			CfnInstance gciAws07Mdb01 = null;
			{
				//root 200G IOPS 3000, througput 125
				//ext 300G IOPS 3000, througput 125
				//ext01	100G IOPS 16000, througput 1000
				
				//除了固定的/dev/sda1 /dev/sdb
				//03 /dev/sdf /dev/sdg /dev/sdh /dev/sdi /dev/sdj /dev/sdk
				//   /dev/sdl /dev/sdm /dev/sdn /dev/sdo /dev/sdp /dev/sdd
				//sdd是打錯字?
				//06 /dev/sdc /dev/sdd /dev/sde /dev/sdf /dev/sdg /dev/sdh
				//07 /dev/sdc /dev/sdd /dev/sde /dev/sdf /dev/sdg /dev/sdh
				String mdbName = "gci-aws07-mdb";
				LaunchTemplateTagSpecificationProperty ff = 
					LaunchTemplateTagSpecificationProperty.builder()
					.resourceType("launch-template")
					.tags(Name.of("gci-aws07-db-template-tag-name"))
					.build();
				
				//db這裡不設hostname
				CfnLaunchTemplate lt = CfnLaunchTemplate.Builder.create(this, "myLaunchTemplate01")
					.tagSpecifications(List.of(ff))
					.launchTemplateName("gci-aws07-db-template")
					.launchTemplateData(LaunchTemplateDataProperty.builder()
						.blockDeviceMappings(List.of(
							software.amazon.awscdk.services.ec2.CfnLaunchTemplate.BlockDeviceMappingProperty.builder()
								.ebs(software.amazon.awscdk.services.ec2.CfnLaunchTemplate.EbsProperty.builder()
//									.volumeSize(200)//03
//									.iops(3000)
//									.volumeType("gp3")
									
									.volumeSize(10)//測試
//									.deleteOnTermination(false)//因為防呆?
									.deleteOnTermination(true)//TODO:測試時不要防呆
									.build())
								.deviceName("/dev/sda1")
								.build()
							, software.amazon.awscdk.services.ec2.CfnLaunchTemplate.BlockDeviceMappingProperty.builder()
								.ebs(software.amazon.awscdk.services.ec2.CfnLaunchTemplate.EbsProperty.builder()
//									.volumeSize(300)//03
//									.iops(3000)
//									.volumeType("gp3")

									.volumeSize(8)//TODO:測試
									.volumeType("gp2")//TODO:測試
									
//									.deleteOnTermination(false)//因為防呆?
									.deleteOnTermination(true)//TODO:測試時不要防呆
									.build())
								.deviceName("/dev/sdb")//對應tag name ext
								.build()
							, software.amazon.awscdk.services.ec2.CfnLaunchTemplate.BlockDeviceMappingProperty.builder()
								.ebs(software.amazon.awscdk.services.ec2.CfnLaunchTemplate.EbsProperty.builder()
//									.volumeSize(100)//03、06要6顆
//									.iops(3000)
//									.volumeType("gp3")
									
									.throughput(500)
									.volumeSize(8)//TODO:測試
									.volumeType("gp3")//TODO:測試
									
//									.deleteOnTermination(false)//因為防呆?
									.deleteOnTermination(true)//TODO:測試時不要防呆
									.build())
								//如果沒給，不會自動取得?好難用
								.deviceName("/dev/sdc")//對應tag name ext01
								.build()
							))
//						.instanceType("r5.8xlarge")//03
//						.instanceType("r5.4xlarge")//06
						.instanceType("t3.small")
		                .userData(Fn.base64(Fn.join("\n", List.of(
							"#!/bin/bash",
							"sed -i 's/\\<PasswordAuthentication no\\>/PasswordAuthentication yes/g' /etc/ssh/sshd_config", 
							"sed -i '$aPermitRootLogin yes' /etc/ssh/sshd_config",
							"sed -i '$aPort 56123' /etc/ssh/sshd_config",
							"service sshd restart",
							"mkdir /oradata",
							"mkfs -t xfs /dev/nvme1n1",
							"mount /dev/nvme1n1 /oradata"
//							"sudo hostnamectl set-hostname --static "+mdbName//有很多方法，這個方法比較簡單
						))))
		                 .build())
					.build();
				
				LaunchTemplateSpecificationProperty ltp = LaunchTemplateSpecificationProperty.builder()
					.launchTemplateName("gci-aws07-db-template")
					.version("1")
					.build();
				
				CfnInstance myGciAws07Mdb01 = CfnInstance.Builder.create(this, "myGciAws07Mdb01")
					.tags(Name.of(mdbName))
					.subnetId(privateA.getRef())
					.availabilityZone(az1)
					.securityGroupIds(List.of(securityGroupWebGroup.getRef()))
//					.instanceType("r5.8xlarge")//aws03
//					.instanceType("r5.4xlarge")//aws06
					.instanceType("t3.micro")//TODO:測試
					.monitoring(false)
					.privateIpAddress("172.18.2.110")
					.imageId(ami03)//ca
					.keyName(keyPairs)
					.launchTemplate(ltp)
//					.blockDeviceMappings(blockDeviceMappings)
					.build();
				gciAws07Mdb01 = myGciAws07Mdb01;
				
				myGciAws07Mdb01.addDependsOn(lt);
				

				{//mdb cpu
					GraphWidget graphWidget = GraphWidget.Builder.create()
			        	.view(GraphWidgetView.TIME_SERIES)
			        	.region(region)
			        	.statistic("Average")
			        	.period(Duration.minutes(1))
			        	.title("DB-CPU")
			        	.build();
					mdbCpu = graphWidget;
			        
					graphWidget.addLeftMetric(Metric.Builder.create()
						.metricName("CPUUtilization")
			            .namespace("AWS/EC2")
//			            .statistic("Average")
			            .account(account)
			            .dimensionsMap(new HashMap<String, String>() {{
			                put("InstanceId", myGciAws07Mdb01.getRef());
			            }})
			            .region(region)
			            .label(myGciAws07Mdb01.getTags().tagValues().get("Name"))
						.build());
				}
				{
					GraphWidget graphWidget = GraphWidget.Builder.create()
			        	.view(GraphWidgetView.TIME_SERIES)
			        	.region(region)
			        	.statistic("Average")
			        	.period(Duration.minutes(1))
			        	.title("DB-NETWORK")
			        	.build();
					mdbNetwork = graphWidget;
			        
					graphWidget.addLeftMetric(Metric.Builder.create()
						.metricName("NetworkOut")
			            .namespace("AWS/EC2")
			            .account(account)
			            .dimensionsMap(new HashMap<String, String>() {{
			                put("InstanceId", myGciAws07Mdb01.getRef());
			            }})
			            .region(region)
			            .label(myGciAws07Mdb01.getTags().tagValues().get("Name")+"NetworkOut")
						.build());
					
					graphWidget.addLeftMetric(Metric.Builder.create()
						.metricName("NetworkIn")
			            .namespace("AWS/EC2")
			            .account(account)
			            .dimensionsMap(new HashMap<String, String>() {{
			                put("InstanceId", myGciAws07Mdb01.getRef());
			            }})
			            .region(region)
			            .label(myGciAws07Mdb01.getTags().tagValues().get("Name")+"NetworkIn")
						.build());
				}
				{
					GraphWidget graphWidget = GraphWidget.Builder.create()
			        	.view(GraphWidgetView.TIME_SERIES)
			        	.region(region)
			        	.statistic("Average")
			        	.period(Duration.minutes(1))
			        	.title("DB-NETWORK-PACKETS")
			        	.build();
					mdbNetworkPackets = graphWidget;
			        
					graphWidget.addLeftMetric(Metric.Builder.create()
						.metricName("NetworkPacketsOut")
			            .namespace("AWS/EC2")
			            .account(account)
			            .dimensionsMap(new HashMap<String, String>() {{
			                put("InstanceId", myGciAws07Mdb01.getRef());
			            }})
			            .region(region)
			            .label(myGciAws07Mdb01.getTags().tagValues().get("Name")+"NetworkPacketsOut")
						.build());
					
					graphWidget.addLeftMetric(Metric.Builder.create()
						.metricName("NetworkPacketsIn")
			            .namespace("AWS/EC2")
			            .account(account)
			            .dimensionsMap(new HashMap<String, String>() {{
			                put("InstanceId", myGciAws07Mdb01.getRef());
			            }})
			            .region(region)
			            .label(myGciAws07Mdb01.getTags().tagValues().get("Name")+"NetworkPacketsIn")
						.build());
				}
//				{
//					//拿不到= =
//					try {
//						traceJsiiNode(myGciAws07Mdb01.$jsii$toJson());	
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//					GraphWidget graphWidget = GraphWidget.Builder.create()
//			        	.view(GraphWidgetView.TIME_SERIES)
//			        	.region(region)
//			        	.statistic("Average")
//			        	.period(Duration.minutes(1))
//			        	.title("DB-READ-OPS")
//			        	.build();
//					mdbVolumeReadOps = graphWidget;
//			        
//					//再加上/dev/sdb
//					//03 /dev/sdf /dev/sdg /dev/sdh /dev/sdi /dev/sdj /dev/sdk
//					//   /dev/sdl /dev/sdm /dev/sdn /dev/sdo /dev/sdp /dev/sdd
//					
////					aws ec2 describe-volumes --filters Name=attachment.instance-id,Values=i-0bcf961098bb8510e --profile test2 --query "Volumes[0].Attachments[0].Device"
////					aws ec2 describe-volumes --filters Name=attachment.instance-id,Values=i-0bcf961098bb8510e --profile test2 --query "Volumes[0].Attachments[0].VolumeId"

//				}
			}
			CfnInstance gciAws07Wdb = null;
			{
				String wdbName = "gci-aws07-wdb";
				CfnInstance myGciAws07Wdb = CfnInstance.Builder.create(this, "myGciAws07Wdb")
					.tags(Name.of(wdbName))
					.subnetId(privateA.getRef())
					.availabilityZone(az1)
					.securityGroupIds(List.of(securityGroupWebGroup.getRef()))
//					.instanceType("m5.2xlarge")
//					.instanceType("m5.large")//TODO:測試
					.instanceType("t3.micro")//TODO:測試
					.monitoring(false)
					.privateIpAddress("172.18.2.112")
					.imageId(ami03)//ca
					.keyName(keyPairs)
					.blockDeviceMappings(List.of(
						BlockDeviceMappingProperty.builder()
							.ebs(EbsProperty.builder()
//								.volumeSize(100)
								.volumeSize(10)
//								.deleteOnTermination(false)//因為防呆?
								.deleteOnTermination(true)//TODO:測試時不要防呆
								.build())
							.deviceName("/dev/sda1")
							.build()
						, BlockDeviceMappingProperty.builder()
							.ebs(EbsProperty.builder()
//								.volumeSize(100)
//								.iops(3000)
								.volumeSize(8)//TODO:測試
//								.iops(100)//TODO:測試
//								.volumeType("gp3")//TODO:測試
								.volumeType("gp2")
//								.deleteOnTermination(false)//因為防呆?
								.deleteOnTermination(true)//TODO:測試時不要防呆
								.build())
							.deviceName("/dev/sdb")
							.build()
						))
						.userData(Fn.base64(Fn.join("\n", List.of(
							"#!/bin/bash",
							"sed -i 's/\\<PasswordAuthentication no\\>/PasswordAuthentication yes/g' /etc/ssh/sshd_config", 
							"sed -i '$aPermitRootLogin yes' /etc/ssh/sshd_config",
							"sed -i '$aPort 56123' /etc/ssh/sshd_config",
//							"sed -i '$aPort 22' /etc/ssh/sshd_config",
							"service sshd restart",
							"mkdir /oradata",
							"mkfs -t xfs /dev/nvme1n1",
							"mount /dev/nvme1n1 /oradata"
//							"sudo hostnamectl set-hostname --static "+wdbName//有很多方法，這個方法比較簡單
						))))
					.build();
				gciAws07Wdb = myGciAws07Wdb;
			}
			
//			gciAws07web01.getTags().tagValues().get("Name");
//			CfnOutput.Builder.create(this, web01Name+"TagName").value(gciAws07web01.getTags().tagValues().get("Name")).build();
			
			//監控的部分，會有一個新的群組概念
			java.util.List<CfnInstance> allApServerList = List.of(gciAws07api01, gciAws07web01);
			java.util.List<CfnInstance> apiServerList = List.of(gciAws07api01);
			java.util.List<CfnInstance> webServerList = List.of(gciAws07web01);
			java.util.List<CfnInstance> dbServerList = List.of(gciAws07Mdb01, gciAws07Wdb);
			java.util.List<CfnInstance> redisServerList = List.of(gciAws07Redis01);
			
//			https://docs.aws.amazon.com/zh_tw/AmazonCloudWatch/latest/monitoring/graph_a_metric.html
//			使用者指南和console都是叫Graphed metrics(圖形化指標)
//			但API這裡命名為GraphWidget(圖形小工具?)
			{//all ap
				GraphWidget graphWidget = GraphWidget.Builder.create()
		        	.view(GraphWidgetView.TIME_SERIES)
		        	.region(region)
		        	.statistic("Average")
		        	.period(Duration.minutes(1))
		        	.title("AP-CPU")
		        	.build();
				apCpu = graphWidget;
		        
				for(CfnInstance instance : allApServerList) {
					graphWidget.addLeftMetric(Metric.Builder.create()
						.metricName("CPUUtilization")
			            .namespace("AWS/EC2")
			            .account(account)
			            .dimensionsMap(new HashMap<String, String>() {{
			                put("InstanceId", instance.getRef());
			            }})
			            .region(region)
			            .label(instance.getTags().tagValues().get("Name"))
						.build());
				}
			}
			{//all ap
				
				GraphWidget graphWidget = GraphWidget.Builder.create()
		        	.view(GraphWidgetView.TIME_SERIES)
		        	.region(region)
		        	.statistic("Average")
		        	.period(Duration.minutes(5))
		        	.title("AP-MEMORY")
		        	.build();
				apMemory = graphWidget;
		        
				for(CfnInstance instance : allApServerList) {
					graphWidget.addLeftMetric(Metric.Builder.create()
						.metricName("UsedMemoryPercent")
			            .namespace("AWS/EC2")
			            .account(account)
			            .dimensionsMap(new HashMap<String, String>() {{
			                put("InstanceId", instance.getRef());
			            }})
			            .region(region)
			            .label(instance.getTags().tagValues().get("Name"))
						.build());
				}
			}
			{//api					
				GraphWidget graphWidget = GraphWidget.Builder.create()
		        	.view(GraphWidgetView.TIME_SERIES)
		        	.region(region)
		        	.statistic("Average")
		        	.period(Duration.minutes(1))
		        	.title("API NETWORK")
		        	.build();
				apiNetwork = graphWidget;
		        
				for(CfnInstance instance : apiServerList) {
					graphWidget.addLeftMetric(Metric.Builder.create()
						.metricName("NetworkOut")
			            .namespace("AWS/EC2")
			            .account(account)
			            .dimensionsMap(new HashMap<String, String>() {{
			                put("InstanceId", instance.getRef());
			            }})
			            .region(region)
			            .label(instance.getTags().tagValues().get("Name")+"NetworkOut")
						.build());
					
					graphWidget.addLeftMetric(Metric.Builder.create()
						.metricName("NetworkIn")
			            .namespace("AWS/EC2")
			            .account(account)
			            .dimensionsMap(new HashMap<String, String>() {{
			                put("InstanceId", instance.getRef());
			            }})
			            .region(region)
			            .label(instance.getTags().tagValues().get("Name")+"NetworkIn")
						.build());
				}
			}
			{//web
				GraphWidget graphWidget = GraphWidget.Builder.create()
		        	.view(GraphWidgetView.TIME_SERIES)
		        	.region(region)
		        	.statistic("Average")
		        	.period(Duration.minutes(1))
		        	.title("WEB-NETWORK")
		        	.build();
				webNetwork = graphWidget;
		        
				for(CfnInstance instance : webServerList) {
					graphWidget.addLeftMetric(Metric.Builder.create()
						.metricName("NetworkOut")
			            .namespace("AWS/EC2")
			            .account(account)
			            .dimensionsMap(new HashMap<String, String>() {{
			                put("InstanceId", instance.getRef());
			            }})
			            .region(region)
			            .label(instance.getTags().tagValues().get("Name")+"NetworkOut")
						.build());
					
					graphWidget.addLeftMetric(Metric.Builder.create()
						.metricName("NetworkIn")
			            .namespace("AWS/EC2")
			            .account(account)
			            .dimensionsMap(new HashMap<String, String>() {{
			                put("InstanceId", instance.getRef());
			            }})
			            .region(region)
			            .label(instance.getTags().tagValues().get("Name")+"NetworkIn")
						.build());
				}
			}
			{//all db network
				GraphWidget graphWidget = GraphWidget.Builder.create()
		        	.view(GraphWidgetView.TIME_SERIES)
		        	.region(region)
		        	.statistic("Average")
		        	.period(Duration.minutes(1))
		        	.title("DB-NETWORK")
		        	.build();
				dbNetwork = graphWidget;
		        
				for(CfnInstance instance : dbServerList) {
					graphWidget.addLeftMetric(Metric.Builder.create()
						.metricName("NetworkOut")
			            .namespace("AWS/EC2")
			            .account(account)
			            .dimensionsMap(new HashMap<String, String>() {{
			                put("InstanceId", instance.getRef());
			            }})
			            .region(region)
			            .label(instance.getTags().tagValues().get("Name")+"NetworkOut")
						.build());
					
					graphWidget.addLeftMetric(Metric.Builder.create()
						.metricName("NetworkIn")
			            .namespace("AWS/EC2")
			            .account(account)
			            .dimensionsMap(new HashMap<String, String>() {{
			                put("InstanceId", instance.getRef());
			            }})
			            .region(region)
			            .label(instance.getTags().tagValues().get("Name")+"NetworkIn")
						.build());
				}
			}
			{//all db cpu
				GraphWidget graphWidget = GraphWidget.Builder.create()
		        	.view(GraphWidgetView.TIME_SERIES)
		        	.region(region)
		        	.statistic("Average")
		        	.period(Duration.minutes(1))
		        	.title("DB-CPU")
		        	.build();
				dbCpu = graphWidget;
		        
				for(CfnInstance instance : dbServerList) {
					graphWidget.addLeftMetric(Metric.Builder.create()
						.metricName("CPUUtilization")
			            .namespace("AWS/EC2")
//			            .statistic("Average")
			            .account(account)
			            .dimensionsMap(new HashMap<String, String>() {{
			                put("InstanceId", instance.getRef());
			            }})
			            .region(region)
			            .label(instance.getTags().tagValues().get("Name"))
						.build());
				}
			}
			{
				//redisServerList是預設計
				GraphWidget graphWidget = GraphWidget.Builder.create()
		        	.view(GraphWidgetView.TIME_SERIES)
		        	.region(region)
		        	.statistic("Average")
		        	.period(Duration.minutes(1))
		        	.title("REDIS-CPU")
		        	.build();
				redisCpu = graphWidget;
		        
				for(CfnInstance instance : redisServerList) {
					graphWidget.addLeftMetric(Metric.Builder.create()
						.metricName("CPUUtilization")
			            .namespace("AWS/EC2")
//			            .statistic("Average")
			            .account(account)
			            .dimensionsMap(new HashMap<String, String>() {{
			                put("InstanceId", instance.getRef());
			            }})
			            .region(region)
			            .label(instance.getTags().tagValues().get("Name"))
						.build());
				}
			}
		}
		
//		https://docs.aws.amazon.com/zh_tw/elasticloadbalancing/latest/application/introduction.html
		//ALB
		CfnLoadBalancer gciAws07ApiAlb = null;
		CfnTargetGroup gciAws07ApiAlbTargetGroup = null;
		{
			CfnLoadBalancer myElb01 = CfnLoadBalancer.Builder.create(this, "myElb01")
				.securityGroups(List.of(securityGroupWebElb.getRef()))
				.name("gci-aws07-api-alb")
				.scheme("internet-facing")
				.ipAddressType("ipv4")
				.subnetMappings(List.of(
					SubnetMappingProperty.builder()
					.subnetId(publicA.getRef())
					.build(),
					SubnetMappingProperty.builder()
					.subnetId(publicB.getRef())
					.build()
				))
				.type("application")
				.build();
			gciAws07ApiAlb = myElb01;
			
			CfnTargetGroup myApi01TargetGroup = CfnTargetGroup.Builder.create(this, "myApi01TargetGroup")
		         .healthCheckIntervalSeconds(25)
		         .healthCheckPath("/img.png")
		         .healthCheckPort("80")
		         .healthCheckProtocol("HTTP")
		         .healthCheckTimeoutSeconds(20)	
		         .healthyThresholdCount(2)
		         .unhealthyThresholdCount(2)
		         .ipAddressType("ipv4")
		         .matcher(MatcherProperty.builder().httpCode("200").build())
		         .name("gci-aws07-api01-targetgroup")//暫時叫這個
		         .port(80)
		         .protocol("HTTP")
		         .protocolVersion("HTTP1")
		         .vpcId(vpc.getRef())
		         .targetGroupAttributes(List.of(
		        	 TargetGroupAttributeProperty.builder()
		             .key("deregistration_delay.timeout_seconds")
		             .value("0").build(),
		             TargetGroupAttributeProperty.builder()
		             .key("load_balancing.algorithm.type")
		             .value("least_outstanding_requests").build()))
		         .build();
			gciAws07ApiAlbTargetGroup = myApi01TargetGroup;
			
//			https://docs.aws.amazon.com/zh_tw/elasticloadbalancing/latest/application/create-https-listener.html
			CfnListener.Builder.create(this, "myElbListener1")
				.defaultActions(List.of(
					ActionProperty.builder()
						.type("forward")
						.targetGroupArn(gciAws07ApiAlbTargetGroup.getRef())
						.build()
				))
				.port(80)
				.protocol("HTTP")
				.loadBalancerArn(gciAws07ApiAlb.getRef())
				.build();
		}
		//只看api的alb
		java.util.List<CfnLoadBalancer> apiAlbList = List.of(gciAws07ApiAlb);
		GraphWidget requestCount = null;
		{
//			java.util.List<java.util.List<? extends IWidget>> widgets = new java.util.ArrayList<>();
			GraphWidget graphWidget = GraphWidget.Builder.create()
	        	.view(GraphWidgetView.TIME_SERIES)
	        	.region(region)
	        	.statistic("Sum")
	        	.period(Duration.minutes(1))
	        	.title("RequestCount")
	        	.build();
			requestCount = graphWidget;
	        
			for(CfnLoadBalancer alb : apiAlbList) {
				graphWidget.addLeftMetric(Metric.Builder.create()
					.metricName("RequestCount")
		            .namespace("AWS/ApplicationELB")
//		            .statistic("Sum")
//		            .statistic(statistic)
		            .account(account)
		            .dimensionsMap(new HashMap<String, String>() {{
		                put("LoadBalancer", alb.getAttrLoadBalancerFullName());
		            }})
		            .region(region)
//		            .label(gciAws07ApiAlb.getTags().tagValues().get("Name"))
		            .label(alb.getAttrLoadBalancerName())
					.build());
			}
//			widgets.add(List.of(graphWidget));
		}
		GraphWidget apiErrorCount = null;
		{
//			API-ErrorCount
			GraphWidget graphWidget = GraphWidget.Builder.create()
	        	.view(GraphWidgetView.TIME_SERIES)
	        	.region(region)
	        	.statistic("Sum")
	        	.period(Duration.minutes(1))
	        	.title("API-ErrorCount")
	        	.build();
			apiErrorCount = graphWidget;
	        
			for(CfnLoadBalancer alb : apiAlbList) {
				graphWidget.addLeftMetric(Metric.Builder.create()
					.metricName("HTTPCode_Target_4XX_Count")
		            .namespace("AWS/ApplicationELB")
		            .account(account)
		            .dimensionsMap(new HashMap<String, String>() {{
		                put("LoadBalancer", alb.getAttrLoadBalancerFullName());
		            }})
		            .region(region)
//		            .label(gciAws07ApiAlb.getTags().tagValues().get("Name"))
//		            .label(gciAws07ApiAlb.getAttrLoadBalancerName())
		            .label("Target_4XX_Count")
					.build());
				
				graphWidget.addLeftMetric(Metric.Builder.create()
					.metricName("HTTPCode_Target_5XX_Count")
		            .namespace("AWS/ApplicationELB")
		            .account(account)
		            .dimensionsMap(new HashMap<String, String>() {{
		                put("LoadBalancer", alb.getAttrLoadBalancerFullName());
		            }})
		            .region(region)
		            .label("Target_5XX_Count")
					.build());
				
				graphWidget.addLeftMetric(Metric.Builder.create()
					.metricName("HTTPCode_ELB_502_Count")
		            .namespace("AWS/ApplicationELB")
		            .account(account)
		            .dimensionsMap(new HashMap<String, String>() {{
		                put("LoadBalancer", alb.getAttrLoadBalancerFullName());
		            }})
		            .region(region)
		            .label("ELB_502_Count")
					.build());
				
				graphWidget.addLeftMetric(Metric.Builder.create()
					.metricName("HTTPCode_ELB_5XX_Count")
		            .namespace("AWS/ApplicationELB")
		            .account(account)
		            .dimensionsMap(new HashMap<String, String>() {{
		                put("LoadBalancer", alb.getAttrLoadBalancerFullName());
		            }})
		            .region(region)
		            .label("ELB_5XX_Count")
					.build());
				
				graphWidget.addLeftMetric(Metric.Builder.create()
					.metricName("HTTPCode_ELB_4XX_Count")
		            .namespace("AWS/ApplicationELB")
		            .account(account)
		            .dimensionsMap(new HashMap<String, String>() {{
		                put("LoadBalancer", alb.getAttrLoadBalancerFullName());
		            }})
		            .region(region)
		            .label("ELB_4XX_Count")
					.build());
			}
		}
		

		//https://docs.aws.amazon.com/zh_tw/global-accelerator/latest/dg/what-is-global-accelerator.html
		//GA
		CfnAccelerator cfnAccelerator = null;
		GraphWidget gciAws07APIGA = null;//ga目前是分別顯示
		{
			CfnAccelerator myCfnAccelerator = CfnAccelerator.Builder.create(this, "myCfnAccelerator")
		         .name("gciawc07apialbCaAccelerator")
		         .enabled(true)
		         .ipAddressType("IPV4")
		         .build();
			cfnAccelerator = myCfnAccelerator;
			
			software.amazon.awscdk.services.globalaccelerator.CfnListener cfnListener = software.amazon.awscdk.services.globalaccelerator.CfnListener.Builder.create(this, "MyCfnListener")
		         .acceleratorArn(cfnAccelerator.getRef())
		         .portRanges(List.of(
		        	 software.amazon.awscdk.services.globalaccelerator.CfnListener.PortRangeProperty.builder()
		                 .fromPort(80)
		                 .toPort(80)
		                 .build()))
		         .protocol("TCP")
		         .build();
			
			software.amazon.awscdk.services.globalaccelerator.CfnEndpointGroup cfnEndpointGroup = software.amazon.awscdk.services.globalaccelerator.CfnEndpointGroup.Builder.create(this, "myGAEndpoint01")
		         .endpointGroupRegion(region)//ca
		         .listenerArn(cfnListener.getRef())
		         // the properties below are optional
		         .endpointConfigurations(List.of(
		        	 software.amazon.awscdk.services.globalaccelerator.CfnEndpointGroup.EndpointConfigurationProperty.builder()
		                 .endpointId(gciAws07ApiAlb.getRef())
		                 .weight(100)
		                 .build()))
		         .healthCheckPath("/img.png")
		         .healthCheckPort(80)
		         .build();
			
			{
				GraphWidget graphWidget = GraphWidget.Builder.create()
		        	.view(GraphWidgetView.TIME_SERIES)
		        	.region(region)
		        	.statistic("Average")
		        	.period(Duration.minutes(1))
//		        	.title("API NETWORK")
		        	.title(myCfnAccelerator.getName())
		        	.build();
				gciAws07APIGA = graphWidget;
		        
				graphWidget.addLeftMetric(Metric.Builder.create()
					.metricName("ProcessedBytesOut")
		            .namespace("AWS/GlobalAccelerator")
		            .account(account)
		            .dimensionsMap(new HashMap<String, String>() {{
		                put("Accelerator", myCfnAccelerator.getAttrAcceleratorArn());
		            }})
		            .region(region)
					.build());
				
				graphWidget.addLeftMetric(Metric.Builder.create()
					.metricName("ProcessedBytesIn")
		            .namespace("AWS/GlobalAccelerator")
		            .account(account)
		            .dimensionsMap(new HashMap<String, String>() {{
		                put("Accelerator", myCfnAccelerator.getAttrAcceleratorArn());
		            }})
		            .region(region)
					.build());
			}
		}
		
		//https://docs.aws.amazon.com/zh_tw/elasticloadbalancing/latest/classic/introduction.html
		//CLB
		software.amazon.awscdk.services.elasticloadbalancing.CfnLoadBalancer gciAws07WebClb = null;
		{
			software.amazon.awscdk.services.elasticloadbalancing.CfnLoadBalancer myClb01 = software.amazon.awscdk.services.elasticloadbalancing.CfnLoadBalancer.Builder.create(this, "myClb01")
		         .listeners(List.of(software.amazon.awscdk.services.elasticloadbalancing.CfnLoadBalancer.ListenersProperty.builder()
		                 .instancePort("80")
		                 .loadBalancerPort("80")
		                 .protocol("HTTP")
		                 // the properties below are optional
		                 .instanceProtocol("HTTP")
		                 .build()))
		         .appCookieStickinessPolicy(List.of(software.amazon.awscdk.services.elasticloadbalancing.CfnLoadBalancer.AppCookieStickinessPolicyProperty.builder()
		                 .cookieName("JSESSIONID")
		                 .policyName("AppCookieStickinessPolicy")
		                 .build()))
		         .connectionDrainingPolicy(software.amazon.awscdk.services.elasticloadbalancing.CfnLoadBalancer.ConnectionDrainingPolicyProperty.builder()
		                 .enabled(true)
		                 // the properties below are optional
		                 .timeout(300)
		                 .build())
		         .connectionSettings(software.amazon.awscdk.services.elasticloadbalancing.CfnLoadBalancer.ConnectionSettingsProperty.builder()
		                 .idleTimeout(60)
		                 .build())
		         .crossZone(true)
		         .healthCheck(software.amazon.awscdk.services.elasticloadbalancing.CfnLoadBalancer.HealthCheckProperty.builder()
		                 .healthyThreshold("2")
		                 .interval("25")
		                 .target("HTTP:80/img.png")
		                 .timeout("20")
		                 .unhealthyThreshold("2")
		                 .build())
//		         .instances(List.of(gciAws07web01.getRef()))
		         .loadBalancerName("gci-aws07-web-clb")
		         .scheme("internet-facing")
		         .securityGroups(List.of(securityGroupWebElb.getRef()))
		         .subnets(List.of(publicA.getRef(), publicB.getRef()))
		         .build();
			gciAws07WebClb = myClb01;
		}
		
//		https://docs.aws.amazon.com/zh_tw/AmazonCloudFront/latest/DeveloperGuide/Introduction.html
		//CloudFront
		
		CfnDistribution agDistribution = null;
		GraphWidget cloudfrontWeb = null;//只有一個CLB
		GraphWidget cloudfrontWebRequest = null;//目前是這樣
		{
			//TODO:等有備份，再記Cookie logging，註06沒開
			//TODO:備用domain是記錄的，還是真的有作用?如果是真的有作用，gciag那個cf應該是設錯了，因為gci不能用
			//cachePolicyId和originRequestPolicyId需同時使用
			String originId = gciAws07WebClb.getRef() + "-origin";
			CfnDistribution myCfnDistribution01 = CfnDistribution.Builder.create(this, "myCfnDistribution01")
				.distributionConfig(
					DistributionConfigProperty.builder()
					.comment("usplaynet.com--web")
					.httpVersion("http1.1")
					.enabled(true)
					.cacheBehaviors(List.of(CacheBehaviorProperty.builder().targetOriginId(originId)
		                 .pathPattern("/theme/images/*")
		                 .viewerProtocolPolicy("allow-all")
		                 .compress(true)
		                 .allowedMethods(List.of("GET", "HEAD", "OPTIONS", "PUT", "POST", "PATCH", "DELETE"))
		                 .cachedMethods(List.of("GET", "HEAD", "OPTIONS"))
		                 .cachePolicyId(MANAGED_CACHINGOPTIMIZED)
		                 .originRequestPolicyId(MANAGED_ELEMENTAL_MEDIATAILOR_PERSONALIZEDMANIFESTS)
		                 .build()))
					.defaultCacheBehavior(DefaultCacheBehaviorProperty.builder().targetOriginId(originId)
						.viewerProtocolPolicy("allow-all")
						.compress(true)
						.allowedMethods(List.of("GET", "HEAD", "OPTIONS", "PUT", "POST", "PATCH", "DELETE"))
						.cachedMethods(List.of("GET", "HEAD", "OPTIONS"))
						.cachePolicyId(MANAGED_CACHINGDISABLED)
						.originRequestPolicyId(MANAGED_ALLVIEWER)
						.build())
					.priceClass("PriceClass_All")
					.origins(List.of(OriginProperty.builder().id(originId)//06沒有自訂
		                .domainName(gciAws07WebClb.getAttrDnsName())
		                .customOriginConfig(CustomOriginConfigProperty.builder()
		                	.originProtocolPolicy("http-only")
		                	.httpPort(80)
		                	.build())
		                .build()))
					.build())
				.build();	
			agDistribution = myCfnDistribution01;
			
			{
				GraphWidget graphWidget = GraphWidget.Builder.create()
		        	.view(GraphWidgetView.TIME_SERIES)
		        	.region(region)
		        	.statistic("Average")
		        	.period(Duration.minutes(1))
		        	.title("CLOUDFRONT-WEB")
		        	.build();
				cloudfrontWeb = graphWidget;
		        
				//https://docs.aws.amazon.com/AmazonCloudFront/latest/DeveloperGuide/programming-cloudwatch-metrics.html
				//us-east-1是固定的
				graphWidget.addLeftMetric(Metric.Builder.create()
					.metricName("BytesUploaded")
		            .namespace("AWS/CloudFront")
		            .account(account)
		            .dimensionsMap(new HashMap<String, String>() {{
		                put("DistributionId", myCfnDistribution01.getAttrId());
		                put("region", "Global");//奇怪?
		            }})
		            .region("us-east-1")
					.build());
				
				graphWidget.addLeftMetric(Metric.Builder.create()
					.metricName("BytesDownloaded")
		            .namespace("AWS/CloudFront")
		            .account(account)
		            .dimensionsMap(new HashMap<String, String>() {{
		                put("DistributionId", myCfnDistribution01.getAttrId());
		                put("region", "Global");//奇怪?
		            }})
		            .region("us-east-1")
					.build());
			}
			{
				GraphWidget graphWidget = GraphWidget.Builder.create()
		        	.view(GraphWidgetView.TIME_SERIES)
		        	.region(region)
		        	.statistic("Average")
		        	.period(Duration.minutes(1))
		        	.title("CLOUDFRONT-WEB-REQUESTS")
		        	.build();
				cloudfrontWebRequest = graphWidget;
		        
				//https://docs.aws.amazon.com/AmazonCloudFront/latest/DeveloperGuide/programming-cloudwatch-metrics.html
				//us-east-1是固定的
				graphWidget.addLeftMetric(Metric.Builder.create()
					.metricName("Requests")
		            .namespace("AWS/CloudFront")
		            .account(account)
		            .dimensionsMap(new HashMap<String, String>() {{
		                put("DistributionId", myCfnDistribution01.getAttrId());
		                put("region", "Global");//奇怪?
		            }})
		            .region("us-east-1")
					.build());
			}
		}
		
//		https://docs.aws.amazon.com/zh_tw/AmazonS3/latest/userguide/Welcome.html
//		S3
		CfnBucket myBucket = null;
		{
			CfnBucket myFirstBucket = CfnBucket.Builder.create(this, "myFirstBucket")
				.bucketName("aws07-gamehall-ca")
				.websiteConfiguration(WebsiteConfigurationProperty.builder()
					.indexDocument("index.html")
					.build())
				.corsConfiguration(CorsConfigurationProperty.builder()
					.corsRules(List.of(CorsRuleProperty.builder()
						.allowedHeaders(List.of("*"))
						.allowedMethods(List.of("GET", "HEAD", "PUT", "POST"))
						.allowedOrigins(List.of("*"))
						.exposedHeaders(List.of())
						.build()))
					.build())
//				))
		    	.build();
			myBucket = myFirstBucket;
			
//			https://docs.aws.amazon.com/zh_tw/AmazonS3/latest/userguide/access-policy-language-overview.html
			CfnBucketPolicy cfnBucketPolicy = CfnBucketPolicy.Builder.create(this, "myCfnBucketPolicy")
		         .bucket(myBucket.getRef())
		         .policyDocument(PolicyDocument.Builder.create()
		     		.statements(List.of(PolicyStatement.Builder.create()
		     			//當需要獨立S3 API IAM時，可考慮參考06
		     			//arn:aws:iam::326781879425:user/aws06_s3
		    			.principals(List.of(new AnyPrincipal()))//先簡單做
		    			.actions(List.of("s3:GetObject"))
		    			.resources(List.of(myBucket.getAttrArn()+"/*"))//不明白為何要/*
		    			.build()))
		    		.build())
		         .build();
			
			//這個可以上傳，但會建立奇怪的Lambda Function?
			IBucket websiteBucket = Bucket.fromBucketName(this, "myDeployBucket01", myBucket.getBucketName());
			BucketDeployment deployment = BucketDeployment.Builder.create(this, "myDeployWebsite01")
				.sources(List.of(Source.asset(s3Dir)))
				.destinationBucket(websiteBucket)
				.build();
		}
		CfnDistribution s3Distribution = null;
		GraphWidget cloudfrontS3 = null;//只有一個S3
		{//s3://awccabucket-aws07
			String s3OriginId = myBucket.getRef() + "-s3-origin";
			CfnDistribution myCfnDistribution02 = CfnDistribution.Builder.create(this, "myCfnDistribution02")
				.distributionConfig(
					DistributionConfigProperty.builder()
					.comment("aws07-gamehall-s3")
					.httpVersion("http1.1")
					.enabled(true)
					.defaultCacheBehavior(DefaultCacheBehaviorProperty.builder()
						.targetOriginId(s3OriginId)
						.allowedMethods(List.of("GET", "HEAD", "OPTIONS", "PUT", "POST", "PATCH", "DELETE"))
						.cachedMethods(List.of("GET", "HEAD", "OPTIONS"))
						.compress(true)
		                .defaultTtl(86400).maxTtl(86400).minTtl(0)
		                .forwardedValues(ForwardedValuesProperty.builder()
		                	.queryString(true)
		                	.cookies(CookiesProperty.builder().forward("all").build())
		                	//API做不出All
		                	.headers(List.of("Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers"))
		                	.build())
						.viewerProtocolPolicy("allow-all")
						.build())
					.priceClass("PriceClass_All")
					.origins(List.of(OriginProperty.builder().id(s3OriginId)//06沒有自訂
		                .domainName(myBucket.getAttrDomainName())//測試再說
		                .originPath("/gameHall")
		                .customOriginConfig(CustomOriginConfigProperty.builder()
	                        .originProtocolPolicy("http-only")
	                        .httpPort(80)
	                        .build())
		                .build()))
					.build())
				.build();	
			s3Distribution = myCfnDistribution02;
			
			{
				GraphWidget graphWidget = GraphWidget.Builder.create()
		        	.view(GraphWidgetView.TIME_SERIES)
		        	.region(region)
		        	.statistic("Average")
		        	.period(Duration.minutes(1))
		        	.title("CLOUDFRONT-S3")
		        	.build();
				cloudfrontS3 = graphWidget;
		        
				graphWidget.addLeftMetric(Metric.Builder.create()
					.metricName("BytesUploaded")
		            .namespace("AWS/CloudFront")
		            .account(account)
		            .dimensionsMap(new HashMap<String, String>() {{
		                put("DistributionId", myCfnDistribution02.getAttrId());
		                put("region", "Global");//奇怪?
		            }})
		            .region("us-east-1")
					.build());
				
				graphWidget.addLeftMetric(Metric.Builder.create()
					.metricName("BytesDownloaded")
		            .namespace("AWS/CloudFront")
		            .account(account)
		            .dimensionsMap(new HashMap<String, String>() {{
		                put("DistributionId", myCfnDistribution02.getAttrId());
		                put("region", "Global");//奇怪?
		            }})
		            .region("us-east-1")
					.build());
			}
		}
		
		//https://docs.aws.amazon.com/zh_tw/Route53/latest/DeveloperGuide/Welcome.html
		{//route53
			CfnHostedZone cfnHostedZone = CfnHostedZone.Builder.create(this, "myCfnHostedZone")
				.name("usplaynet2.com")
				.build();
			
			CfnRecordSet samgaRecordSet = CfnRecordSet.Builder.create(this, "myCfnRecordSet01")
				.name("samga.usplaynet2.com")
				.type("A")
				.aliasTarget(CfnRecordSet.AliasTargetProperty.builder()
					.dnsName(cfnAccelerator.getAttrDnsName())
					.hostedZoneId(GA_ZONE_ID)
					.build())
				.hostedZoneId(cfnHostedZone.getRef())
				.build();
			
			CfnRecordSet samcfRecordSet = CfnRecordSet.Builder.create(this, "myCfnRecordSet02")
				.name("samcf.usplaynet2.com")
				.type("A")
				.aliasTarget(CfnRecordSet.AliasTargetProperty.builder()
					.dnsName(agDistribution.getAttrDomainName())
					.hostedZoneId(CF_ZONE_ID)
					.build())
				.hostedZoneId(cfnHostedZone.getRef())
				.build();
			
			CfnRecordSet sams3RecordSet = CfnRecordSet.Builder.create(this, "myCfnRecordSet03")
		         .name("sams3.usplaynet2.com")
		         .type("A")
		         .aliasTarget(CfnRecordSet.AliasTargetProperty.builder()
		        	 .dnsName(s3Distribution.getAttrDomainName())
		        	 .hostedZoneId(CF_ZONE_ID)
		             .build())
		         .hostedZoneId(cfnHostedZone.getRef())
		         .build();
		}
		
//		https://docs.aws.amazon.com/zh_tw/AmazonCloudWatch/latest/monitoring/CloudWatch_Dashboards.html
//		CloudWatch Dashboard
		{
			//有些東西是用memreport.sh生成的
			//cloudwatch這裡暫時先不要動
			//另有scp.sh
			//sysctl.conf做系統優化
			
//			https://docs.aws.amazon.com/zh_tw/AmazonCloudWatch/latest/monitoring/edit_graph_dashboard.html
			java.util.List<java.util.List<? extends IWidget>> widgets = new java.util.ArrayList<>();
			//這個寫法和排版有關，要注意
			widgets.add(List.of(apCpu, apMemory, requestCount, apiErrorCount));
			widgets.add(List.of(apiNetwork, webNetwork, dbNetwork, dbCpu));
			widgets.add(List.of(redisCpu));
			Dashboard.Builder.create(this, "myDashboard01")
				.dashboardName("AWC-Server")
				.widgets(widgets)
				.build();
			
			java.util.List<java.util.List<? extends IWidget>> widgets2 = new java.util.ArrayList<>();
			widgets2.add(List.of(cloudfrontWeb, cloudfrontWebRequest));
			widgets2.add(List.of(cloudfrontS3));
			Dashboard.Builder.create(this, "myDashboard02")
				.dashboardName("Cloudfront-LB")
				.widgets(widgets2)
				.build();
			
			java.util.List<java.util.List<? extends IWidget>> widgets3 = new java.util.ArrayList<>();
			widgets3.add(List.of(mdbCpu));
			widgets3.add(List.of(mdbNetwork, mdbNetworkPackets));
			Dashboard.Builder.create(this, "myDashboard03")
				.dashboardName("gci-aws07-mdb")
				.widgets(widgets3)
				.build();
			
			java.util.List<java.util.List<? extends IWidget>> widgets4 = new java.util.ArrayList<>();
			widgets4.add(List.of(gciAws07APIGA));
//			widgets4.add(List.of(gciAws07APIGA, gciAws07WEBGA));
//			widgets4.add(List.of(gciAws07FETCHGA));
			Dashboard.Builder.create(this, "myDashboard04")
				.dashboardName("GolbalAccelerator")
				.widgets(widgets4)
				.build();
		}
		
		//https://docs.aws.amazon.com/zh_tw/AmazonCloudWatch/latest/monitoring/US_SetupSNS.html
		//SNS with EMail or Telegram
		CfnTopic cfnTopic = null;
		ITopic topic = null;
		{
			CfnTopic myCfnTopic01 = CfnTopic.Builder.create(this, "myCfnTopic01")
		         .fifoTopic(false)
		         .displayName("gci-awc07-sns2")
		         .topicName("gci-awc07-sns2")//刪了再建
		         .build();
			cfnTopic = myCfnTopic01;
			
//			//需取得權限，先跳過，或者Kai哥幫忙驗證
//			//這裡有一個BUG
//			//https://stackoverflow.com/questions/61994055/aws-sns-subcriptions-vs-triggers-on-lambda-function
//			CfnSubscription cfnSubscription = CfnSubscription.Builder.create(this, "myCfnSubscription01")
//		        .protocol("lambda")
//		        .topicArn(cfnTopic.getRef())
////		        https://docs.aws.amazon.com/sns/latest/api/API_GetSubscriptionAttributes.html
//		        .endpoint(authorizerFn.getFunctionArn())
//		        .build();
			
//			CfnSubscription cfnSubscription = CfnSubscription.Builder.create(this, "myCfnSubscription01")
//	        .protocol("email")
//	        .topicArn(cfnTopic.getRef())
////	        https://docs.aws.amazon.com/sns/latest/api/API_GetSubscriptionAttributes.html
//	        //.endpoint("phawca@aws101.aliass.xyz")//TODO:測試不要用這個
////	        .endpoint("samleetw0213@gmail.com")//不試了
//	        .build();
			//註，有關pending confirme
			//https://aws.amazon.com/sns/faqs/?nc1=h_ls
			//Token included in the confirmation message sent to end-points 
			//on a subscription request are valid for 3 days.
			
			//系統手動建的，比較細
//			{
//			    "Version": "2012-10-17",
//			    "Statement": [
//			        {
//			            "Effect": "Allow",
//			            "Action": "logs:CreateLogGroup",
//			            "Resource": "arn:aws:logs:us-west-1:481311441598:*"
//			        },
//			        {
//			            "Effect": "Allow",
//			            "Action": [
//			                "logs:CreateLogStream",
//			                "logs:PutLogEvents"
//			            ],
//			            "Resource": [
//			                "arn:aws:logs:us-west-1:481311441598:log-group:/aws/lambda/tg02:*"
//			            ]
//			        }
//			    ]
//			}
//			自訂的比較寬
//			{
//			    "Version": "2012-10-17",
//			    "Statement": [
//			        {
//			            "Action": [
//			                "logs:CreateLogGroup",
//			                "logs:CreateLogStream",
//			                "logs:PutLogEvents"
//			            ],
//			            "Resource": "arn:aws:logs:*:*:*",
//			            "Effect": "Allow"
//			        }
//			    ]
//			}
			
//			aws07 for test
//			5202455986:AAFr4jtthiniLDkS3WYS44UbMGqFhOMPbcs
//			1166271913		
//			aws06
//			770859310:AAERstjYp9IwbFAG4yGGwXQsXEdvabxSSAg
//			-680978662
//			//AQWCNET -測試環境通知	awcsms_bot
//			NOTIFY("770859310:AAERstjYp9IwbFAG4yGGwXQsXEdvabxSSAg", "-399926738"),
			
			//相對自訂Policy，這個好像比較簡單，不要搞得很複雜
			//會由stack建一個policy，並指到系統預設的AWSLambdaBasicExecutionRole(寬的)
			//主要參考的原文是
			//https://cloudbriefly.com/post/forwarding-amazon-sns-notifications-to-a-telegram-chat/
			
			//TODO:下面網址示範，如何修改發通知的訊息，在程式的message之後，parse.urlencode之前
			//https://blog.1seo.top/wiki/Python/%E5%B0%86AmazonSNS%E9%80%9A%E7%9F%A5%E8%BD%AC%E5%8F%91%E5%88%B0Telegram/
			Function authorizerFn = Function.Builder.create(this, "myAuthorizerFunction01")
		         .runtime(software.amazon.awscdk.services.lambda.Runtime.PYTHON_3_7)
		         .handler("lambda_function.lambda_handler")
		         .code(AssetCode.fromAsset("asset"))
		         .environment(new HashMap<String, String>() {{
		                put("CHAT_ID", "1166271913");
		                put("TOKEN", "5202455986:AAFr4jtthiniLDkS3WYS44UbMGqFhOMPbcs");
		            }})
		         .functionName("gci-aws07-telegram-function")
		         .timeout(Duration.seconds(5))
		         .build();
			
			ITopic myTopic01 = Topic.fromTopicArn(this, "myTopic01", cfnTopic.getRef());
			topic = myTopic01;
			
			myTopic01.addSubscription(LambdaSubscription.Builder.create(authorizerFn)
				.build());
		}
		
		//跟Email無關，但他名字叫AlarmThatSendsEmail
//		https://docs.aws.amazon.com/AmazonCloudWatch/latest/monitoring/AlarmThatSendsEmail.html
//		CloudWatch alarms
		{
			software.amazon.awscdk.services.elasticloadbalancing.CfnLoadBalancer gciAws07WebClbRef = gciAws07WebClb;
					
			
			//還沒有action時，長這樣，先記錄一下
			CfnAlarm cfnAlarm02 = CfnAlarm.Builder.create(this, "myCfnAlarm02")
				.alarmName(gciAws07WebClbRef.getLoadBalancerName()+" Unhealthy2")
				.comparisonOperator("GreaterThanThreshold")
				.threshold(0)
				.evaluationPeriods(1)
				.metrics(List.of(MetricDataQueryProperty.builder()
					.id("shit001")
					.metricStat(MetricStatProperty.builder()
                        .metric(MetricProperty.builder()
                        	.dimensions(List.of(DimensionProperty.builder()
                        		.name("LoadBalancerName")
                        		.value(gciAws07WebClbRef.getLoadBalancerName())
                        		.build()))
                        	.metricName("UnHealthyHostCount")
                        	.namespace("AWS/ELB")
                        	.build())
                        .period(300)
                        .stat("Average")
                        .build())
					.build()))
					.okActions(List.of(topic.getTopicArn()))
					.alarmActions(List.of(topic.getTopicArn()))
				.build();
			
//			這裡的pending confirm可能是舊的那個卡住了
//			{"AlarmName":"gci-aws07-web01 CPU over 60%","AlarmDescription":null,"AWSAccountId":"481311441598","AlarmConfigurationUpdatedTimestamp":"2022-03-11T11:02:15.617+0000","NewStateValue":"OK","NewStateReason":"Threshold Crossed: 3 datapoints [0.050002640067216934 (11/03/22 10:58:00), 0.04333311112870379 (11/03/22 10:53:00), 0.03166697224583412 (11/03/22 10:48:00)] were not greater than the threshold (60.0).","StateChangeTime":"2022-03-11T11:03:03.943+0000","Region":"US West (N. California)","AlarmArn":"arn:aws:cloudwatch:us-west-1:481311441598:alarm:gci-aws07-web01 CPU over 60%","OldStateValue":"INSUFFICIENT_DATA","Trigger":{"MetricName":"CPUUtilization","Namespace":"AWS/EC2","StatisticType":"ExtendedStatistic","ExtendedStatistic":"Average","Unit":null,"Dimensions":[{"value":"i-0b8582b3ccdd2c421","name":"InstanceId"}],"Period":300,"EvaluationPeriods":3,"ComparisonOperator":"GreaterThanThreshold","Threshold":60.0,"TreatMissingData":"","EvaluateLowSampleCountPercentile":""}}
//			這裡應該要手動調整訊息
//			https://aws.amazon.com/tw/blogs/mt/customize-amazon-cloudwatch-alarm-notifications-to-your-local-time-zone-part-1/
				
			CfnInstance gciAws07web01Ref = gciAws07web01;
			CfnAlarm cfnAlarm04 = CfnAlarm.Builder.create(this, "myCfnAlarm04")
				.alarmName(gciAws07web01Ref.getTags().tagValues().get("Name")+" memory greate 90%")
				.alarmDescription(gciAws07web01Ref.getTags().tagValues().get("Name")+" memory greate 90%")
				.comparisonOperator("GreaterThanThreshold")
				.threshold(90)
				.evaluationPeriods(3)
				.metrics(List.of(MetricDataQueryProperty.builder()
					.id("shit004")
					.metricStat(MetricStatProperty.builder()
	                    .metric(MetricProperty.builder()
	                    	.dimensions(List.of(DimensionProperty.builder()
	                    		.name("InstanceId")
	                    		.value(gciAws07web01Ref.getRef())
	                    		.build()))
	                    	.metricName("UsedMemoryPercent")
	                    	.namespace("AWS/EC2")
	                    	.build())
	                    .period(300)
	                    .stat("Average")
	                    .build())
					.build()))
					.okActions(List.of(topic.getTopicArn()))
					.alarmActions(List.of(topic.getTopicArn()))
				.build();
		
			CfnAlarm cfnAlarm05 = CfnAlarm.Builder.create(this, "myCfnAlarm05")
				.alarmName(gciAws07web01Ref.getTags().tagValues().get("Name")+" CPU over 60%")
				.alarmDescription(gciAws07web01Ref.getTags().tagValues().get("Name")+" CPU over 60%")
				.comparisonOperator("GreaterThanThreshold")//超過
				.threshold(60)//60
				.evaluationPeriods(3)//3個資料點
//				.datapointsToAlarm(4)//4個取3個的意思，所以會變成4*300=20分鐘
				.metrics(List.of(MetricDataQueryProperty.builder()
					.id("shit005")
					.metricStat(MetricStatProperty.builder()
	                    .metric(MetricProperty.builder()
	                    	.dimensions(List.of(DimensionProperty.builder()
	                    		.name("InstanceId")
	                    		.value(gciAws07web01Ref.getRef())
	                    		.build()))
	                    	.metricName("CPUUtilization")//我是一個百分比
	                    	.namespace("AWS/EC2")
	                    	.build())
	                    .period(300)//5分鐘，應該要1分鐘的吧?
	                    .stat("Average")
	                    .build())
					.build()))
					.okActions(List.of(topic.getTopicArn()))
					.alarmActions(List.of(topic.getTopicArn()))
				.build();
			
//			Metric mmm = Metric.Builder.create()
//			.metricName("CPUUtilization")
//            .namespace("AWS/EC2")
//            .statistic("Average")
//            .account(account)
//            .dimensionsMap(new HashMap<String, String>() {{
//                put("InstanceId", gciAws07web01Ref.getRef());
//            }})
//            .region(region)
//            .label(gciAws07web01Ref.getTags().tagValues().get("Name"))
//			.build();
//			
//			AlarmProps alarmProps = AlarmProps.builder()
//				.alarmName(gciAws07web01Ref.getTags().tagValues().get("Name")+" CPU over 70%")
//				.alarmDescription(gciAws07web01Ref.getTags().tagValues().get("Name")+" CPU over 70%")
//				.comparisonOperator(ComparisonOperator.GREATER_THAN_THRESHOLD)
//				.threshold(50)
//				.evaluationPeriods(3)
//				.metric(mmm)
//				.actionsEnabled(true)
//				.build();
			
//			CreateAlarmOptions.builder().alarmDescription(alarmDescription).build();
//			Alarm alarm = new Alarm(this, "myAlarm03", alarmProps);
//			alarm.addOkAction(cfnTopic);
//			alarm.addOkAction(AlarmActionConfig.builder().alarmActionArn(alarmActionArn).build());
		}
		
//		https://docs.aws.amazon.com/zh_tw/aws-backup/latest/devguide/whatisbackup.html
//		AWS Backup
		//EC2可備份及複製成功
		{
			//奇怪的文章，最下兩行說不能刪?
			//https://aws.amazon.com/tw/premiumsupport/knowledge-center/efs-disable-automatic-backups/
			//TODO:
			//建立備份庫
			//建立備份計畫
			//選EC2，選1天
			//註，手動建的很好刪
			
			//隨需的在這裡
			CfnBackupVault cfnBackupVault = CfnBackupVault.Builder.create(this, "myCfnBackupVault01")
				.backupVaultName("gci-aws07-bak-vault")
				.accessPolicy(PolicyDocument.Builder.create()
					.statements(List.of(PolicyStatement.Builder.create()
						.sid("aws07CdkPolicy02")
						.effect(Effect.ALLOW)
						.principals(List.of(new StarPrincipal()))
						.actions(List.of("backup:DeleteBackupVault",
			                "backup:DeleteBackupVaultAccessPolicy",
			                "backup:DeleteRecoveryPoint",
			                "backup:StartCopyJob",
			                "backup:StartRestoreJob",
			                "backup:UpdateRecoveryPointLifecycle"
							))
						.resources(List.of("*"))
						.build()))
					.build())
				.build();
			
			CfnBackupPlan cfnBackupPlan = CfnBackupPlan.Builder.create(this, "myCfnBackupPlan01")
				.backupPlan(BackupPlanResourceTypeProperty.builder()
					.backupPlanName("gci-aws07-bak-plan")
					.backupPlanRule(List.of(BackupRuleResourceTypeProperty.builder()
						.ruleName("gci-aws07-bak-rule")
						.targetBackupVault(cfnBackupVault.getRef())
						//如果不設startWindowMinutes、scheduleExpression、completionWindowMinutes
						//1.會是5點開始啟動
						//2.沒有啟動成功的，8點前會取消
						//3.有啟動成功的沒做完的，7天內會取消
						
						//測試 --start
//						.startWindowMinutes(60)//建立後60分鐘開始
//						.scheduleExpression("cron(5 * ? * * *)")//每整點5分，測試用
//						.completionWindowMinutes(2880)//2天
						//測試 --end
						
						//暫時移除copyActions，成功再加回
						.copyActions(List.of(CopyActionResourceTypeProperty.builder()
							.destinationBackupVaultArn(remoteBackup)
							.lifecycle(LifecycleResourceTypeProperty.builder()
								.deleteAfterDays(1)//測試
								.build())
							.build()))//異地備援
						.lifecycle(LifecycleResourceTypeProperty.builder()
							.deleteAfterDays(1)//測試
							.build())//本地備援
						.build()))
					.build())
				.build();
			
//			預設role，會建立一個新的
//			arn:aws:iam::481311441598:role/service-role/AWSBackupDefaultServiceRole
//			內建的backup role
//			arn:aws:iam::481311441598:role/aws-service-role/backup.amazonaws.com/AWSServiceRoleForBackup
			
//			arn:aws:iam::481311441598:role/service-role/AWSBackupDefaultServiceRole
//			arn:aws:iam::481311441598:role/service-role/AWSBackupDefaultServiceRole

			//https://docs.aws.amazon.com/zh_tw/aws-backup/latest/devguide/s3-backups.html
			
			CfnRole aws07BackupRole = CfnRole.Builder.create(this, "myRole01")
				.managedPolicyArns(List.of(
					"arn:aws:iam::aws:policy/service-role/AWSBackupServiceRolePolicyForBackup",
					"arn:aws:iam::aws:policy/service-role/AWSBackupServiceRolePolicyForRestores"))
				.assumeRolePolicyDocument(PolicyDocument.Builder.create()
					.statements(List.of(PolicyStatement.Builder.create()
						.effect(Effect.ALLOW)
						.principals(List.of(ServicePrincipal.Builder.create("backup.amazonaws.com").build()))
						.actions(List.of("sts:AssumeRole"))
						.build()))
					.build())
				//inner s3-backup-policy

//				.policies(List.of(software.amazon.awscdk.services.iam.CfnRole.PolicyProperty.builder()
//					.policyDocument(PolicyDocument.Builder.create()
//						.statements(List.of(
//							PolicyStatement.Builder.create()
//								.sid("S3BucketBackupPermissions")
//								.effect(Effect.ALLOW)
//			//						.principals(List.of(ServicePrincipal.Builder.create("backup.amazonaws.com").build()))
//								.actions(List.of(
//									"s3:GetInventoryConfiguration",
//							        "s3:PutInventoryConfiguration",
//							        "s3:ListBucketVersions",
//							        "s3:ListBucket",
//							        "s3:GetBucketVersioning",
//							        "s3:GetBucketNotification",
//							        "s3:PutBucketNotification",
//							        "s3:GetBucketLocation",
//							        "s3:GetBucketTagging"
//									))
//								.resources(List.of("arn:aws:s3:::*"))
//								.build(),
//							PolicyStatement.Builder.create()
//								.sid("S3ObjectBackupPermissions")
//								.effect(Effect.ALLOW)
//								.actions(List.of(
//									"s3:GetObjectAcl",
//							        "s3:GetObject",
//							        "s3:GetObjectVersionTagging",
//							        "s3:GetObjectVersionAcl",
//							        "s3:GetObjectTagging",
//							        "s3:GetObjectVersion"
//									))
//								.resources(List.of("arn:aws:s3:::*/*"))
//								.build(),
//							PolicyStatement.Builder.create()
//								.sid("S3GlobalPermissions")
//								.effect(Effect.ALLOW)
//								.actions(List.of(
//									"s3:ListAllMyBuckets"
//									))
//								.resources(List.of("*"))
//								.build(),
//							PolicyStatement.Builder.create()
//								.sid("KMSBackupPermissions")
//								.effect(Effect.ALLOW)
//								.actions(List.of(
//									"kms:Decrypt",
//							        "kms:DescribeKey"
//									))
//								.resources(List.of("*"))
//								.conditions(new HashMap<String, Object>() {{
//					                put("StringLike", new HashMap<String, String>() {{
//						                put("kms:ViaService", "s3.*.amazonaws.com");
//						            }});
//					            }})
//								.build(),
//							PolicyStatement.Builder.create()
//								.sid("EventsPermissions")
//								.effect(Effect.ALLOW)
//								.actions(List.of(
//									"events:DescribeRule",
//							        "events:EnableRule",
//							        "events:PutRule",
//							        "events:DeleteRule",
//							        "events:PutTargets",
//							        "events:RemoveTargets",
//							        "events:ListTargetsByRule",
//							        "events:DisableRule"
//									))
//								.resources(List.of("arn:aws:events:*:*:rule/AwsBackupManagedRule*"))
//								.build(),
//							PolicyStatement.Builder.create()
//								.sid("EventsMetricsGlobalPermissions")
//								.effect(Effect.ALLOW)
//								.actions(List.of(
//									"cloudwatch:GetMetricData",
//							        "events:ListRules"
//									))
//								.resources(List.of("*"))
//								.build()))
//						.build())
//					.policyName("aws07-s3-backup-policy")
//					.build(),
//					software.amazon.awscdk.services.iam.CfnRole.PolicyProperty.builder()
//					.policyDocument(PolicyDocument.Builder.create()
//						.statements(List.of(
//							PolicyStatement.Builder.create()
//								.sid("S3BucketRestorePermissions")
//								.effect(Effect.ALLOW)
//								.actions(List.of(
//									"s3:CreateBucket",
//							        "s3:ListBucketVersions",
//							        "s3:ListBucket",
//							        "s3:GetBucketVersioning",
//							        "s3:GetBucketLocation",
//							        "s3:PutBucketVersioning"
//									))
//								.resources(List.of("arn:aws:s3:::*"))
//								.build(),
//							PolicyStatement.Builder.create()
//								.sid("S3ObjectRestorePermissions")
//								.effect(Effect.ALLOW)
//								.actions(List.of(
//									"s3:GetObject",
//							        "s3:GetObjectVersion",
//							        "s3:DeleteObject",
//							        "s3:PutObjectVersionAcl",
//							        "s3:GetObjectVersionAcl",
//							        "s3:GetObjectTagging",
//							        "s3:PutObjectTagging",
//							        "s3:GetObjectAcl",
//							        "s3:PutObjectAcl",
//							        "s3:PutObject",
//							        "s3:ListMultipartUploadParts"
//									))
//								.resources(List.of("arn:aws:s3:::*/*"))
//								.build(),
//							PolicyStatement.Builder.create()
//								.sid("S3KMSPermissions")
//								.effect(Effect.ALLOW)
//								.actions(List.of(
//									"kms:Decrypt",
//							        "kms:DescribeKey",
//							        "kms:GenerateDataKey"
//									))
//								.resources(List.of("*"))
//								.conditions(new HashMap<String, Object>() {{
//					                put("StringLike", new HashMap<String, String>() {{
//						                put("kms:ViaService", "s3.*.amazonaws.com");
//						            }});
//					            }})
//								.build()))
//						.build())
//					.policyName("aws07-s3-restore-policy")
//					.build()))
				
				.roleName("gci-aws07-backup-role")//未來共用是可行的?
				.build();
			
			//這段是正常運作的，但不要浪費錢，先關起來
			
			CfnBackupSelection cfnBackupSelection = CfnBackupSelection.Builder.create(this, "myCfnBackupSelection01")
				.backupPlanId(cfnBackupPlan.getRef())
				.backupSelection(BackupSelectionResourceTypeProperty.builder()
					//arn:aws:ec2:us-west-1:481311441598:instance/i-0b8582b3ccdd2c421
					//不知道怎麼組的，手動做一下就知道了
					//resources與listOfTags是or條件，這個二擇一的設計不太好用
					//最後選Infra的做法用tag，至於用什麼名，可以再調整
//					.resources(List.of("arn:aws:ec2:"+region+":"+account+":instance/"+gciAws07web01.getRef()))
//					.resources(List.of("arn:aws:ec2:"+region+":"+account+":instance/*"))
					.listOfTags(List.of(
						ConditionResourceTypeProperty.builder()
							.conditionType("STRINGEQUALS")
							.conditionKey("backup")
							.conditionValue("true")
							.build()//這裡即使成功，也不是唯一解吧?
						))//
					//預設應該可以，但測試不順
//					.iamRoleArn("arn:aws:iam::"+account+":role/service-role/AWSBackupDefaultServiceRole")
					.iamRoleArn(aws07BackupRole.getAttrArn())
					.selectionName("gci-aws07-bak-selection")
					.build())
				.build();
		}
		
		//https://docs.aws.amazon.com/zh_tw/efs/latest/ug/whatisefs.html
		//EFS
		{
//			參考文件
//			https://docs.aws.amazon.com/zh_tw/efs/latest/ug/installing-amazon-efs-utils.html
//			centos屬於RPM軟體包，需另安裝aws的套件
//			不使用aws套件的話，看這篇
//			https://docs.aws.amazon.com/zh_tw/efs/latest/ug/mounting-fs-old.html
			
//			註:aws03的AP會在/etc/rc.local裡初始化，df -h會長下面這樣
//			test01:
//			fs-0bfc5dc346f087bee.efs.ap-northeast-1.amazonaws.com:/  8.0E  380G  8.0E   1% /testefs
//			api01:
//			fs-0bfc5dc346f087bee.efs.ap-northeast-1.amazonaws.com:/gci-aws03-api01  8.0E  380G  8.0E   1% /aws03logbackup
			
//			手動做吧，在test01上
//			sudo mkdir /testefs
//			//建一些ec2的資料夾，略
//			sudo mount -t nfs4 -o nfsvers=4.1,rsize=1048576,wsize=1048576,hard,timeo=600,retrans=2,noresvport fs-03d2d80536382a260.efs.us-west-1.amazonaws.com:/ /testefs
//			-sudo umount /testefs
//
//			sudo mkdir /aws03logbackup
//			sudo mount -t nfs4 -o nfsvers=4.1,rsize=1048576,wsize=1048576,hard,timeo=600,retrans=2,noresvport fs-03d2d80536382a260.efs.us-west-1.amazonaws.com:/gci-aws07-web01 /aws03logbackup
//			-sudo umount /aws03logbackup
			
			java.util.List<String> ec2List = List.of(securityGroupTestGroup.getRef()
				, securityGroupApiGroup.getRef()
				, securityGroupWebGroup.getRef());
			
			CfnFileSystem cfnFileSystem = CfnFileSystem.Builder.create(this, "myCfnFileSystem01")
				.fileSystemTags(List.of(ElasticFileSystemTagProperty.builder()
					.key("Name").value("gci-aws07-efs").build()))
				.backupPolicy(BackupPolicyProperty.builder().status("DISABLED").build())
				.build();
			
			CfnMountTarget cfnMountTarget01 = CfnMountTarget.Builder.create(this, "myCfnMountTarget01")
				.fileSystemId(cfnFileSystem.getRef())
				.securityGroups(ec2List)
				.subnetId(test01.getRef())
				.build();
			
			CfnMountTarget cfnMountTarget02 = CfnMountTarget.Builder.create(this, "myCfnMountTarget02")
				.fileSystemId(cfnFileSystem.getRef())
				.securityGroups(ec2List)
				.subnetId(test02.getRef())
				.build();
			
			CfnOutput.Builder.create(this, "efsDoman").value("sudo mount -t nfs4 -o nfsvers=4.1,rsize=1048576,wsize=1048576,hard,timeo=600,retrans=2,noresvport "+cfnFileSystem.getRef()+".efs."+(region)+".amazonaws.com:/ /testefs").build();
		}
		
		//https://docs.aws.amazon.com/zh_tw/IAM/latest/UserGuide/id_groups.html
		//IAM group
		CfnGroup aws07ReadonlyGroup = null;
		{
			CfnGroup aws07Readonly = CfnGroup.Builder.create(this, "aws07Readonly")
				.managedPolicyArns(List.of(
					"arn:aws:iam::aws:policy/AmazonEC2ReadOnlyAccess",
					"arn:aws:iam::aws:policy/CloudWatchEventsReadOnlyAccess",
					"arn:aws:iam::aws:policy/CloudWatchLogsReadOnlyAccess",
					"arn:aws:iam::aws:policy/IAMUserChangePassword",
					"arn:aws:iam::aws:policy/CloudWatchReadOnlyAccess"))
				.groupName("aws07-readonly")
				.build();
			aws07ReadonlyGroup = aws07Readonly;
			
			//參考https://docs.aws.amazon.com/elasticloadbalancing/latest/userguide/load-balancer-authentication-access-control.html
//			The ARN for a Classic Load Balancer has the format shown in the following example.
//			arn:aws:elasticloadbalancing:region-code:account-id:loadbalancer/load-balancer-name
//			The ARN for a target group has the format shown in the following example.
//			arn:aws:elasticloadbalancing:region-code:account-id:targetgroup/target-group-name/target-group-id
			CfnPolicy.Builder.create(this, "myPolicy01")
				.policyDocument(PolicyDocument.Builder.create()
					.statements(List.of(PolicyStatement.Builder.create()
						.sid("aws07CdkPolicy01")
						.actions(List.of("elasticloadbalancing:DescribeSSLPolicies",
								"elasticloadbalancing:DescribeTags",
								"elasticloadbalancing:RegisterTargets",
								"elasticloadbalancing:DescribeLoadBalancerPolicyTypes",
								"elasticloadbalancing:DeregisterTargets",
								"elasticloadbalancing:DescribeLoadBalancerAttributes",
								"elasticloadbalancing:DescribeLoadBalancers",
								"elasticloadbalancing:DescribeTargetGroupAttributes",
								"elasticloadbalancing:DescribeListeners",
								"elasticloadbalancing:DescribeAccountLimits",
								"elasticloadbalancing:DescribeLoadBalancerPolicies",
								"elasticloadbalancing:DescribeTargetHealth",
								"elasticloadbalancing:DescribeTargetGroups",
								"elasticloadbalancing:DescribeListenerCertificates",
								"elasticloadbalancing:DescribeRules",
								"elasticloadbalancing:DescribeInstanceHealth",
								"elasticloadbalancing:DeregisterInstancesFromLoadBalancer",
								"elasticloadbalancing:RegisterInstancesWithLoadBalancer"
							))
						.resources(List.of(
	//							"arn:aws:elasticloadbalancing:us-west-1:481311441598:loadbalancer/gci-aws07-web-clb",
	//							"arn:aws:elasticloadbalancing:"+this.getRegion()+":"+this.getAccount()+":loadbalancer/"+gciAws07WebClb.getLoadBalancerName(),
								"arn:aws:elasticloadbalancing:"+region+":"+account+":loadbalancer/"+gciAws07WebClb.getLoadBalancerName(),
								gciAws07ApiAlbTargetGroup.getRef()
							))
						.build()))
					.build())
				.policyName("aws07-alb-api-register-targets-group")//應該是aws07才對，上面的不對
				.groups(List.of(aws07ReadonlyGroup.getRef()))
				.build();
		}
		{
			//https://docs.aws.amazon.com/zh_tw/secretsmanager/latest/userguide/intro.html
			Secret templatedSecret = Secret.Builder.create(this, "myTemplatedSecret01")
		         .generateSecretString(SecretStringGenerator.builder()
		                 .secretStringTemplate("{\"username\":\"davin146\"}")
		                 .generateStringKey("password")
		                 .build())
		         .build();
			//https://docs.aws.amazon.com/zh_tw/IAM/latest/UserGuide/id_users.html
//			 // Using the templated secret
			 User davin146 = User.Builder.create(this, "myOtherUser01")
				     .userName(templatedSecret.secretValueFromJson("username").toString())
			         .password(templatedSecret.secretValueFromJson("password"))
			         .managedPolicies(List.of(ManagedPolicy.fromManagedPolicyArn(this, 
			        	 "myPolicy02", "arn:aws:iam::aws:policy/IAMUserChangePassword")))
			         .groups(List.of(Group.fromGroupName(this, "myGroup01", aws07ReadonlyGroup.getRef())))
			         .build();
			 //這樣會在自動產生密碼
			//手動aws secretsmanager get-secret-value --secret-id
			 //有權限的人，也可透過console在AWS Secrets Manager
			 //secretsmanager get-secret-value的secret-id要用，getSecretArn或getSecretFullArn長得一樣= =
			CfnOutput.Builder.create(this, "davin146Password").value(templatedSecret.getSecretArn()).build();
		}
		{
			Secret templatedSecret = Secret.Builder.create(this, "myTemplatedSecret02")
		         .generateSecretString(SecretStringGenerator.builder()
//		                 .secretStringTemplate(JSON.stringify(Map.of("username", "sen21")))
		                 .secretStringTemplate("{\"username\":\"sen21\"}")
		                 .generateStringKey("password")
		                 .build())
		         .build();
			 
//			 // Using the templated secret
			 User sen21 = User.Builder.create(this, "myOtherUser02")
				     .userName(templatedSecret.secretValueFromJson("username").toString())
			         .password(templatedSecret.secretValueFromJson("password"))
			         .managedPolicies(List.of(ManagedPolicy.fromManagedPolicyArn(this, 
			        	 "myPolicy03", "arn:aws:iam::aws:policy/IAMUserChangePassword")))
			         .groups(List.of(Group.fromGroupName(this, "myGroup02", aws07ReadonlyGroup.getRef())))
			         .build();
//			 sen21.getUserName()//解析器覺得這個太動態了，無法做為id
			 //為可CfnOutput的ID，有些寫法可以，有些不能
			CfnOutput.Builder.create(this, "sen21Password").value(templatedSecret.getSecretArn()).build();
		}
		{
			//不能改密碼，會有API
			CfnUser awc_PG_dev = CfnUser.Builder.create(this, "myCfnUser02")
		         .loginProfile(LoginProfileProperty.builder()
		                 .password("12345Ab_")
		                 .build())
		         .managedPolicyArns(List.of(
//		        	 "arn:aws:iam::aws:policy/IAMUserChangePassword",
		        	 "arn:aws:iam::aws:policy/AmazonEC2ReadOnlyAccess",
		        	 "arn:aws:iam::aws:policy/CloudWatchEventsReadOnlyAccess",
		        	 "arn:aws:iam::aws:policy/AmazonRoute53ReadOnlyAccess",
		        	 "arn:aws:iam::aws:policy/AmazonS3ReadOnlyAccess",
		        	 "arn:aws:iam::aws:policy/CloudWatchLogsReadOnlyAccess",
		        	 "arn:aws:iam::aws:policy/CloudFrontReadOnlyAccess",
		        	 "arn:aws:iam::aws:policy/IAMReadOnlyAccess",
		        	 "arn:aws:iam::aws:policy/ElasticLoadBalancingReadOnly",
		        	 "arn:aws:iam::aws:policy/CloudWatchReadOnlyAccess",
		        	 "arn:aws:iam::aws:policy/GlobalAcceleratorReadOnlyAccess"
		        	 ))
		         .userName("awc_PG_dev")
		         .build();
			
			//做到awc_PG_dev就好了
			CfnAccessKey cfnAccessKey01 = CfnAccessKey.Builder.create(this, "cfnAccessKey01")
				.userName(awc_PG_dev.getUserName())
				.build();
			
			cfnAccessKey01.addDependsOn(awc_PG_dev);

			CfnOutput.Builder.create(this, cfnAccessKey01.getUserName()+"AccessKeyId").value(cfnAccessKey01.getRef()).build();
			CfnOutput.Builder.create(this, cfnAccessKey01.getUserName()+"SecretAccessKey").value(cfnAccessKey01.getAttrSecretAccessKey()).build();
//			cdk console會印出
//			Outputs:
//			HelloCdkStack.davin146AccessKeyId = AKIAXAEDFYK7F4OVYGK6
//			HelloCdkStack.davin146SecretAccessKey = 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
		}
		{	
			//03 awc-dev, 06 aws07_s3
			CfnUser aws07_s3 = CfnUser.Builder.create(this, "myCfnUser03")
		         .loginProfile(LoginProfileProperty.builder()
		                 .password("12345Ab_")
		                 .build())
		         .managedPolicyArns(List.of("arn:aws:iam::aws:policy/IAMUserChangePassword"))
//		         .userName("awc-dev")//03
		         .userName("aws07_s3")//06
		         .policies(List.of(PolicyProperty.builder()
	                 .policyDocument(PolicyDocument.Builder.create()
	     				.statements(List.of(
	    					PolicyStatement.Builder.create()
	    						.sid("awcDevS301")
	    						.actions(List.of("s3:ListBucket",
	    								"s3:GetBucketWebsite",
	    								"s3:GetBucketLocation"
	    							))
	    						.resources(List.of(
	    								"arn:aws:s3:::"+myBucket.getBucketName()
	    							))
	    						.build(),
	    					PolicyStatement.Builder.create()
	    						.sid("awcDevS302")
	    						.actions(List.of("s3:PutObject",
	    								"s3:GetObject",
	    								"s3:DeleteObject"
	    							))
	    						.resources(List.of(
//	    							"arn:aws:s3:"+region+":"+account+":"+myBucket.getBucketName(),//03
	    							"arn:aws:s3:::"+myBucket.getBucketName(),//06
	    							"arn:aws:s3:::"+myBucket.getBucketName()+"/*"//06
	    							))
	    						.build(),
	    					PolicyStatement.Builder.create()
	    						.sid("awcDevS303")
	    						.actions(List.of("s3:ListAllMyBuckets"
	    							))
	    						.resources(List.of("*"))
	    						.build()
	    						))
	    				.build())
//	                 .policyName("awc-dev-s3")//03
	                 .policyName("aws07_s3_only")//06
	                 .build()))
		         .build();
			
			CfnAccessKey cfnAccessKey02 = CfnAccessKey.Builder.create(this, "cfnAccessKey02")
				.userName(aws07_s3.getUserName())
				.build();
			
			cfnAccessKey02.addDependsOn(aws07_s3);

			CfnOutput.Builder.create(this, cfnAccessKey02.getUserName()+"AccessKeyId").value(cfnAccessKey02.getRef()).build();
			CfnOutput.Builder.create(this, cfnAccessKey02.getUserName()+"SecretAccessKey").value(cfnAccessKey02.getAttrSecretAccessKey()).build();
		}
		//=================================================================
	}
	
	//測試用
	public static void traceJsiiObject(Object obj) throws IOException {
	    JsiiObject J = (JsiiObject) obj;
	    JsonNode jsonNode = (JsonNode) J.$jsii$toJson();
	    ObjectMapper mapper = new ObjectMapper();
	    Object json = mapper.readValue(jsonNode.toString(), Object.class);
	    System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json));
	}
	public static void traceJsiiNode(TreeNode jsonNode) throws IOException {
//	    JsiiObject J = (JsiiObject) obj;
//	    JsonNode jsonNode = (JsonNode) J.$jsii$toJson();
	    ObjectMapper mapper = new ObjectMapper();
	    System.out.println("debug:"+jsonNode.toString());
	    Object json = mapper.readValue(jsonNode.toString(), Object.class);
	    System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json));
	}
}

class List {
	//先簡單自已做一個List.of
	public static <T> java.util.List<T> of(T... args) {
		java.util.List<T> list = new ArrayList<T>();
		for(T arg : args) {
			list.add(arg);
		}
		return list;
	}
}
class Name {
	public static <T> java.util.List<CfnTag> of(String name) {
		return List.of(CfnTag.builder().key("Name").value(name).build());
	}
}
class CfnSecurityGroupIngressBug {
	public static <T> IngressProperty of(String cidrIp, String description) {
		return of("-1", -1, cidrIp, description);
	}
	
	public static <T> IngressProperty of(String ipProtocol, Number port, String cidrIp, String description) {
		
		return of(ipProtocol, port, port, cidrIp, description);
	}
	
	public static <T> IngressProperty of(String ipProtocol, Number fromPort, Number toPort, String cidrIp, String description) {
		return IngressProperty.builder()
			.ipProtocol(ipProtocol)
			.fromPort(fromPort)
			.toPort(toPort)
			.cidrIp(cidrIp)
			.description(description)
			.build();
	}
}